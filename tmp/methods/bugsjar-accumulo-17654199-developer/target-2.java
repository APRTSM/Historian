  protected static void initMetadataConfig() throws IOException {
    initMetadataConfig(RootTable.ID);
    initMetadataConfig(MetadataTable.ID);

    // ACCUMULO-3077 Set the combiner on accumulo.metadata during init to reduce the likelihood of a race
    // condition where a tserver compacts away Status updates because it didn't see the Combiner configured
    IteratorSetting setting = new IteratorSetting(9, ReplicationTableUtil.COMBINER_NAME, StatusCombiner.class);
    Combiner.setColumns(setting, Collections.singletonList(new Column(MetadataSchema.ReplicationSection.COLF)));
    try {
      for (IteratorScope scope : IteratorScope.values()) {
        String root = String.format("%s%s.%s", Property.TABLE_ITERATOR_PREFIX, scope.name().toLowerCase(), setting.getName());
        for (Entry<String,String> prop : setting.getOptions().entrySet()) {
          TablePropUtil.setTableProperty(MetadataTable.ID, root + ".opt." + prop.getKey(), prop.getValue());
        }
        TablePropUtil.setTableProperty(MetadataTable.ID, root, setting.getPriority() + "," + setting.getIteratorClass());
      }
    } catch (Exception e) {
      log.fatal("Error talking to ZooKeeper", e);
      throw new IOException(e);
    }
  }
  public static void updateFiles(Credentials creds, KeyExtent extent, Collection<String> files, Status stat) {
    if (log.isDebugEnabled()) {
      log.debug("Updating replication status for " + extent + " with " + files + " using " + ProtobufUtil.toString(stat));
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
                log.debug("Writing " + ProtobufUtil.toString(status) + " to metadata table for " + logs);
                // Got some new WALs, note this in the metadata table
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
