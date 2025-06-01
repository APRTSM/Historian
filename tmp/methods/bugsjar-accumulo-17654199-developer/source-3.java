  protected static void initMetadataConfig() throws IOException {
    initMetadataConfig(RootTable.ID);
    initMetadataConfig(MetadataTable.ID);
  }
  public static void updateFiles(Credentials creds, KeyExtent extent, Collection<String> files, Status stat) {
    if (log.isDebugEnabled()) {
      log.debug("Updating replication for " + extent + " with " + files + " using " + ProtobufUtil.toString(stat));
    }
    // TODO could use batch writer, would need to handle failure and retry like update does - ACCUMULO-1294
    if (files.isEmpty()) {
      return;
    }

    Value v = ProtobufUtil.toValue(stat);
    for (String file : files) {
      // TODO Can preclude this addition if the extent is for a table we don't need to replicate
      update(creds, createUpdateMutation(new Path(file), v, extent), extent);
    }
  }
  private int write(final Collection<CommitSession> sessions, boolean mincFinish, Writer writer) throws IOException {
    // Work very hard not to lock this during calls to the outside world
    int currentLogSet = logSetId.get();

    int seq = -1;
    int attempt = 1;
    boolean success = false;
    while (!success) {
      try {
        // get a reference to the loggers that no other thread can touch
        ArrayList<DfsLogger> copy = new ArrayList<DfsLogger>();
        currentLogSet = initializeLoggers(copy);

        // add the logger to the log set for the memory in the tablet,
        // update the metadata table if we've never used this tablet

        if (currentLogSet == logSetId.get()) {
          for (CommitSession commitSession : sessions) {
            if (commitSession.beginUpdatingLogsUsed(copy, mincFinish)) {
              try {
                // Scribble out a tablet definition and then write to the metadata table
                defineTablet(commitSession);
                if (currentLogSet == logSetId.get())
                  tserver.addLoggersToMetadata(copy, commitSession.getExtent(), commitSession.getLogId());
              } finally {
                commitSession.finishUpdatingLogsUsed();
              }

              // Need to release
              KeyExtent extent = commitSession.getExtent();
              if (ReplicationConfigurationUtil.isEnabled(extent, tserver.getTableConfiguration(extent))) {
                Set<String> logs = new HashSet<String>();
                for (DfsLogger logger : copy) {
                  logs.add(logger.getFileName());
                }
                Status status = StatusUtil.fileCreated(System.currentTimeMillis());
                log.debug("Writing " + ProtobufUtil.toString(status) + " to replication table for " + logs);
                // Got some new WALs, note this in the replication table
                ReplicationTableUtil.updateFiles(SystemCredentials.get(), commitSession.getExtent(), logs, status);
              }
            }
          }
        }

        // Make sure that the logs haven't changed out from underneath our copy
        if (currentLogSet == logSetId.get()) {

          // write the mutation to the logs
          seq = seqGen.incrementAndGet();
          if (seq < 0)
            throw new RuntimeException("Logger sequence generator wrapped!  Onos!!!11!eleven");
          ArrayList<LoggerOperation> queuedOperations = new ArrayList<LoggerOperation>(copy.size());
          for (DfsLogger wal : copy) {
            LoggerOperation lop = writer.write(wal, seq);
            if (lop != null)
              queuedOperations.add(lop);
          }

          for (LoggerOperation lop : queuedOperations) {
            lop.await();
          }

          // double-check: did the log set change?
          success = (currentLogSet == logSetId.get());
        }
      } catch (DfsLogger.LogClosedException ex) {
        log.debug("Logs closed while writing, retrying " + attempt);
      } catch (Exception t) {
        if (attempt != 1) {
          log.error("Unexpected error writing to log, retrying attempt " + attempt, t);
        }
        UtilWaitThread.sleep(100);
      } finally {
        attempt++;
      }
      // Some sort of write failure occurred. Grab the write lock and reset the logs.
      // But since multiple threads will attempt it, only attempt the reset when
      // the logs haven't changed.
      final int finalCurrent = currentLogSet;
      if (!success) {
        testLockAndRun(logSetLock, new TestCallWithWriteLock() {

          @Override
          boolean test() {
            return finalCurrent == logSetId.get();
          }

          @Override
          void withWriteLock() throws IOException {
            close();
            closeForReplication(sessions);
          }
        });
      }
    }
    // if the log gets too big, reset it .. grab the write lock first
    logSizeEstimate.addAndGet(4 * 3); // event, tid, seq overhead
    testLockAndRun(logSetLock, new TestCallWithWriteLock() {
      @Override
      boolean test() {
        return logSizeEstimate.get() > maxSize;
      }

      @Override
      void withWriteLock() throws IOException {
        close();
        closeForReplication(sessions);
      }
    });
    return seq;
  }
  public void importMapFiles(long tid, Map<FileRef,DataFileValue> pathsString, boolean setTime) throws IOException {

    final KeyExtent extent = tablet.getExtent();
    String bulkDir = null;

    Map<FileRef,DataFileValue> paths = new HashMap<FileRef,DataFileValue>();
    for (Entry<FileRef,DataFileValue> entry : pathsString.entrySet())
      paths.put(entry.getKey(), entry.getValue());

    for (FileRef tpath : paths.keySet()) {

      boolean inTheRightDirectory = false;
      Path parent = tpath.path().getParent().getParent();
      for (String tablesDir : ServerConstants.getTablesDirs()) {
        if (parent.equals(new Path(tablesDir, tablet.getExtent().getTableId().toString()))) {
          inTheRightDirectory = true;
          break;
        }
      }
      if (!inTheRightDirectory) {
        throw new IOException("Data file " + tpath + " not in table dirs");
      }

      if (bulkDir == null)
        bulkDir = tpath.path().getParent().toString();
      else if (!bulkDir.equals(tpath.path().getParent().toString()))
        throw new IllegalArgumentException("bulk files in different dirs " + bulkDir + " " + tpath);

    }

    if (tablet.getExtent().isRootTablet()) {
      throw new IllegalArgumentException("Can not import files to root tablet");
    }

    synchronized (bulkFileImportLock) {
      Credentials creds = SystemCredentials.get();
      Connector conn;
      try {
        conn = HdfsZooInstance.getInstance().getConnector(creds.getPrincipal(), creds.getToken());
      } catch (Exception ex) {
        throw new IOException(ex);
      }
      // Remove any bulk files we've previously loaded and compacted away
      List<FileRef> files = MetadataTableUtil.getBulkFilesLoaded(conn, extent, tid);

      for (FileRef file : files)
        if (paths.keySet().remove(file))
          log.debug("Ignoring request to re-import a file already imported: " + extent + ": " + file);

      if (paths.size() > 0) {
        long bulkTime = Long.MIN_VALUE;
        if (setTime) {
          for (DataFileValue dfv : paths.values()) {
            long nextTime = tablet.getAndUpdateTime();
            if (nextTime < bulkTime)
              throw new IllegalStateException("Time went backwards unexpectedly " + nextTime + " " + bulkTime);
            bulkTime = nextTime;
            dfv.setTime(bulkTime);
          }
        }
        
        tablet.updatePersistedTime(bulkTime, paths, tid);
      }
    }

    synchronized (tablet) {
      for (Entry<FileRef,DataFileValue> tpath : paths.entrySet()) {
        if (datafileSizes.containsKey(tpath.getKey())) {
          log.error("Adding file that is already in set " + tpath.getKey());
        }
        datafileSizes.put(tpath.getKey(), tpath.getValue());

      }

      tablet.getTabletResources().importedMapFiles();

      tablet.computeNumEntries();
    }

    for (Entry<FileRef,DataFileValue> entry : paths.entrySet()) {
      log.log(TLevel.TABLET_HIST, tablet.getExtent() + " import " + entry.getKey() + " " + entry.getValue());
    }
  }
  void bringMinorCompactionOnline(FileRef tmpDatafile, FileRef newDatafile, FileRef absMergeFile, DataFileValue dfv, CommitSession commitSession, long flushId)
      throws IOException {

    IZooReaderWriter zoo = ZooReaderWriter.getRetryingInstance();
    if (tablet.getExtent().isRootTablet()) {
      try {
        if (!zoo.isLockHeld(tablet.getTabletServer().getLock().getLockID())) {
          throw new IllegalStateException();
        }
      } catch (Exception e) {
        throw new IllegalStateException("Can not bring major compaction online, lock not held", e);
      }
    }

    // rename before putting in metadata table, so files in metadata table should
    // always exist
    do {
      try {
        if (dfv.getNumEntries() == 0) {
          tablet.getTabletServer().getFileSystem().deleteRecursively(tmpDatafile.path());
        } else {
          if (tablet.getTabletServer().getFileSystem().exists(newDatafile.path())) {
            log.warn("Target map file already exist " + newDatafile);
            tablet.getTabletServer().getFileSystem().deleteRecursively(newDatafile.path());
          }

          rename(tablet.getTabletServer().getFileSystem(), tmpDatafile.path(), newDatafile.path());
        }
        break;
      } catch (IOException ioe) {
        log.warn("Tablet " + tablet.getExtent() + " failed to rename " + newDatafile + " after MinC, will retry in 60 secs...", ioe);
        UtilWaitThread.sleep(60 * 1000);
      }
    } while (true);

    long t1, t2;

    // the code below always assumes merged files are in use by scans... this must be done
    // because the in memory list of files is not updated until after the metadata table
    // therefore the file is available to scans until memory is updated, but want to ensure
    // the file is not available for garbage collection... if memory were updated
    // before this point (like major compactions do), then the following code could wait
    // for scans to finish like major compactions do.... used to wait for scans to finish
    // here, but that was incorrect because a scan could start after waiting but before
    // memory was updated... assuming the file is always in use by scans leads to
    // one uneeded metadata update when it was not actually in use
    Set<FileRef> filesInUseByScans = Collections.emptySet();
    if (absMergeFile != null)
      filesInUseByScans = Collections.singleton(absMergeFile);

    // very important to write delete entries outside of log lock, because
    // this metadata write does not go up... it goes sideways or to itself
    if (absMergeFile != null)
      MetadataTableUtil.addDeleteEntries(tablet.getExtent(), Collections.singleton(absMergeFile), SystemCredentials.get());

    Set<String> unusedWalLogs = tablet.beginClearingUnusedLogs();
    boolean replicate = ReplicationConfigurationUtil.isEnabled(tablet.getExtent(), tablet.getTableConfiguration());
    Set<String> logFileOnly = null;
    if (replicate) {
      // unusedWalLogs is of the form host/fileURI, need to strip off the host portion
      logFileOnly = new HashSet<>();
      for (String unusedWalLog : unusedWalLogs) {
        int index = unusedWalLog.indexOf('/');
        if (-1 == index) {
          log.warn("Could not find host component to strip from DFSLogger representation of WAL");
        } else {
          unusedWalLog = unusedWalLog.substring(index + 1);
        }
        logFileOnly.add(unusedWalLog);
      }
    }
    try {
      // the order of writing to metadata and walog is important in the face of machine/process failures
      // need to write to metadata before writing to walog, when things are done in the reverse order
      // data could be lost... the minor compaction start even should be written before the following metadata
      // write is made

      tablet.updateTabletDataFile(commitSession.getMaxCommittedTime(), newDatafile, absMergeFile, dfv, unusedWalLogs, filesInUseByScans, flushId);

      // Mark that we have data we want to replicate
      // This WAL could still be in use by other Tablets *from the same table*, so we can only mark that there is data to replicate,
      // but it is *not* closed
      if (replicate) {
        ReplicationTableUtil.updateFiles(SystemCredentials.get(), tablet.getExtent(), logFileOnly, StatusUtil.openWithUnknownLength());
      }
    } finally {
      tablet.finishClearingUnusedLogs();
    }

    do {
      try {
        // the purpose of making this update use the new commit session, instead of the old one passed in,
        // is because the new one will reference the logs used by current memory...
        
        tablet.getTabletServer().minorCompactionFinished(tablet.getTabletMemory().getCommitSession(), newDatafile.toString(), commitSession.getWALogSeq() + 2);
        break;
      } catch (IOException e) {
        log.error("Failed to write to write-ahead log " + e.getMessage() + " will retry", e);
        UtilWaitThread.sleep(1 * 1000);
      }
    } while (true);

    synchronized (tablet) {
      t1 = System.currentTimeMillis();

      if (datafileSizes.containsKey(newDatafile)) {
        log.error("Adding file that is already in set " + newDatafile);
      }
      
      if (dfv.getNumEntries() > 0) {
        datafileSizes.put(newDatafile, dfv);
      }
      
      if (absMergeFile != null) {
        datafileSizes.remove(absMergeFile);
      }
      
      unreserveMergingMinorCompactionFile(absMergeFile);
      
      tablet.flushComplete(flushId);
      
      t2 = System.currentTimeMillis();
    }

    // must do this after list of files in memory is updated above
    removeFilesAfterScan(filesInUseByScans);

    if (absMergeFile != null)
      log.log(TLevel.TABLET_HIST, tablet.getExtent() + " MinC [" + absMergeFile + ",memory] -> " + newDatafile);
    else
      log.log(TLevel.TABLET_HIST, tablet.getExtent() + " MinC [memory] -> " + newDatafile);
    log.debug(String.format("MinC finish lock %.2f secs %s", (t2 - t1) / 1000.0, tablet.getExtent().toString()));
    long splitSize = tablet.getTableConfiguration().getMemoryInBytes(Property.TABLE_SPLIT_THRESHOLD);
    if (dfv.getSize() > splitSize) {
      log.debug(String.format("Minor Compaction wrote out file larger than split threshold.  split threshold = %,d  file size = %,d", splitSize, dfv.getSize()));
    }
  }
