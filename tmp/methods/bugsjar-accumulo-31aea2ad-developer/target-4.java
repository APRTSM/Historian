  public Path getFullPath(FileType fileType, String path) {
    int colon = path.indexOf(':');
    if (colon > -1) {
      // Check if this is really an absolute path or if this is a 1.4 style relative path for a WAL
      if (fileType == FileType.WAL && path.charAt(colon + 1) != '/') {
        path = path.substring(path.indexOf('/'));
      } else {
        return new Path(path);
      }
    }

    // normalize the path
    Path fullPath = new Path(defaultVolume.getBasePath(), fileType.getDirectory());
    if (path.startsWith("/"))
      path = path.substring(1);
    fullPath = new Path(fullPath, path);

    FileSystem fs = getVolumeByPath(fullPath).getFileSystem();
    return fs.makeQualified(fullPath);
  }
  public static Path getRecoveryPath(VolumeManager fs, Path walPath) throws IOException {
    if (walPath.depth() >= 3 && walPath.toUri().getScheme() != null) {
      // its a fully qualified path
      String uuid = walPath.getName();
      // drop uuid
      walPath = walPath.getParent();
      // recovered 1.4 WALs won't have a server component
      if (!walPath.getName().equals(FileType.WAL.getDirectory())) {
        // drop server
        walPath = walPath.getParent();
      }
  
      if (!walPath.getName().equals(FileType.WAL.getDirectory()))
        throw new IllegalArgumentException("Bad path " + walPath);
  
      // drop wal
      walPath = walPath.getParent();
  
      walPath = new Path(walPath, FileType.RECOVERY.getDirectory());
      walPath = new Path(walPath, uuid);

      return walPath;
    }
  
    throw new IllegalArgumentException("Bad path " + walPath);
  
  }
  int scanServers(String[] walDirs, Map<Path,String> fileToServerMap, Map<String,Path> nameToFileMap) throws Exception {
    Set<String> servers = new HashSet<String>();
    for (String walDir : walDirs) {
      Path walRoot = new Path(walDir);
      FileStatus[] listing = null;
      try {
        listing = fs.listStatus(walRoot);
      } catch (FileNotFoundException e) {
        // ignore dir
      }

      if (listing == null)
        continue;
      for (FileStatus status : listing) {
        String server = status.getPath().getName();
        if (status.isDir()) {
          servers.add(server);
          for (FileStatus file : fs.listStatus(new Path(walRoot, server))) {
            if (isUUID(file.getPath().getName())) {
              fileToServerMap.put(file.getPath(), server);
              nameToFileMap.put(file.getPath().getName(), file.getPath());
            } else {
              log.info("Ignoring file " + file.getPath() + " because it doesn't look like a uuid");
            }
          }
        } else if (isUUID(server)) {
          // old-style WAL are not under a directory
          servers.add("");
          fileToServerMap.put(status.getPath(), "");
          nameToFileMap.put(server, status.getPath());
        } else {
          log.info("Ignoring file " + status.getPath() + " because it doesn't look like a uuid");
        }
      }
    }
    return servers.size();
  }
  private int removeMetadataEntries(Map<String,Path>  nameToFileMap, Map<String, Path> sortedWALogs, GCStatus status) throws IOException, KeeperException,
      InterruptedException {
    int count = 0;
    Iterator<LogEntry> iterator = MetadataTableUtil.getLogEntries(SystemCredentials.get());

    while (iterator.hasNext()) {
      for (String entry : iterator.next().logSet) {
        // old style WALs will have the IP:Port of their logger and new style will either be a Path either absolute or relative, in all cases
        // the last "/" will mark a UUID file name.
        String uuid = entry.substring(entry.lastIndexOf("/") + 1);
        if (!isUUID(uuid)) {
          // fully expect this to be a uuid, if its not then something is wrong and walog GC should not proceed!
          throw new IllegalArgumentException("Expected uuid, but got " + uuid + " from " + entry);
        }

        Path pathFromNN = nameToFileMap.remove(uuid);
        if (pathFromNN != null) {
          status.currentLog.inUse++;
          sortedWALogs.remove(uuid);
        }
        count++;
      }
    }
    return count;
  }
  private Set<String> beginClearingUnusedLogs() {
    Set<String> doomed = new HashSet<String>();

    ArrayList<String> otherLogsCopy = new ArrayList<String>();
    ArrayList<String> currentLogsCopy = new ArrayList<String>();

    // do not hold tablet lock while acquiring the log lock
    logLock.lock();

    synchronized (this) {
      if (removingLogs)
        throw new IllegalStateException("Attempted to clear logs when removal of logs in progress");

      for (DfsLogger logger : otherLogs) {
        otherLogsCopy.add(logger.toString());
        doomed.add(logger.getMeta());
      }

      for (DfsLogger logger : currentLogs) {
        currentLogsCopy.add(logger.toString());
        doomed.remove(logger.getMeta());
      }

      otherLogs = Collections.emptySet();

      if (doomed.size() > 0)
        removingLogs = true;
    }

    // do debug logging outside tablet lock
    for (String logger : otherLogsCopy) {
      log.debug("Logs for memory compacted: " + getExtent() + " " + logger.toString());
    }

    for (String logger : currentLogsCopy) {
      log.debug("Logs for current memory: " + getExtent() + " " + logger);
    }

    for (String logger : doomed) {
      log.debug("Logs to be destroyed: " + getExtent() + " " + logger);
    }

    return doomed;
  }
  private Tablet(final TabletServer tabletServer, final Text location, final KeyExtent extent, final TabletResourceManager trm, final Configuration conf,
      final VolumeManager fs, final List<LogEntry> rawLogEntries, final SortedMap<FileRef,DataFileValue> rawDatafiles, String time,
      final TServerInstance lastLocation, Set<FileRef> scanFiles, long initFlushID, long initCompactID) throws IOException {

    TabletFiles tabletPaths = VolumeUtil.updateTabletVolumes(tabletServer.getLock(), fs, extent, new TabletFiles(location.toString(), rawLogEntries,
        rawDatafiles));

    Path locationPath;

    if (tabletPaths.dir.contains(":")) {
      locationPath = new Path(tabletPaths.dir.toString());
    } else {
      locationPath = fs.getFullPath(FileType.TABLE, extent.getTableId().toString() + tabletPaths.dir.toString());
    }

    final List<LogEntry> logEntries = tabletPaths.logEntries;
    final SortedMap<FileRef,DataFileValue> datafiles = tabletPaths.datafiles;

    this.location = locationPath;
    this.lastLocation = lastLocation;
    this.tabletDirectory = tabletPaths.dir;
    this.conf = conf;
    this.acuTableConf = tabletServer.getTableConfiguration(extent);

    this.fs = fs;
    this.extent = extent;
    this.tabletResources = trm;

    this.lastFlushID = initFlushID;
    this.lastCompactID = initCompactID;

    if (extent.isRootTablet()) {
      long rtime = Long.MIN_VALUE;
      for (FileRef ref : datafiles.keySet()) {
        Path path = ref.path();
        FileSystem ns = fs.getVolumeByPath(path).getFileSystem();
        FileSKVIterator reader = FileOperations.getInstance().openReader(path.toString(), true, ns, ns.getConf(), tabletServer.getTableConfiguration(extent));
        long maxTime = -1;
        try {

          while (reader.hasTop()) {
            maxTime = Math.max(maxTime, reader.getTopKey().getTimestamp());
            reader.next();
          }

        } finally {
          reader.close();
        }

        if (maxTime > rtime) {
          time = TabletTime.LOGICAL_TIME_ID + "" + maxTime;
          rtime = maxTime;
        }
      }
    }
    if (time == null && datafiles.isEmpty() && extent.equals(RootTable.OLD_EXTENT)) {
      // recovery... old root tablet has no data, so time doesn't matter:
      time = TabletTime.LOGICAL_TIME_ID + "" + Long.MIN_VALUE;
    }

    this.tabletServer = tabletServer;
    this.logId = tabletServer.createLogId(extent);

    this.timer = new TabletStatsKeeper();

    setupDefaultSecurityLabels(extent);

    tabletMemory = new TabletMemory();
    tabletTime = TabletTime.getInstance(time);
    persistedTime = tabletTime.getTime();

    acuTableConf.addObserver(configObserver = new ConfigurationObserver() {

      private void reloadConstraints() {
        constraintChecker.set(new ConstraintChecker(acuTableConf));
      }

      @Override
      public void propertiesChanged() {
        reloadConstraints();

        try {
          setupDefaultSecurityLabels(extent);
        } catch (Exception e) {
          log.error("Failed to reload default security labels for extent: " + extent.toString());
        }
      }

      @Override
      public void propertyChanged(String prop) {
        if (prop.startsWith(Property.TABLE_CONSTRAINT_PREFIX.getKey()))
          reloadConstraints();
        else if (prop.equals(Property.TABLE_DEFAULT_SCANTIME_VISIBILITY.getKey())) {
          try {
            log.info("Default security labels changed for extent: " + extent.toString());
            setupDefaultSecurityLabels(extent);
          } catch (Exception e) {
            log.error("Failed to reload default security labels for extent: " + extent.toString());
          }
        }

      }

      @Override
      public void sessionExpired() {
        log.debug("Session expired, no longer updating per table props...");
      }

    });
    
    acuTableConf.getNamespaceConfiguration().addObserver(configObserver);
    
    // Force a load of any per-table properties
    configObserver.propertiesChanged();

    tabletResources.setTablet(this, acuTableConf);
    if (!logEntries.isEmpty()) {
      log.info("Starting Write-Ahead Log recovery for " + this.extent);
      // count[0] = entries used on tablet
      // count[1] = track max time from walog entries wihtout timestamps
      final long[] count = new long[2];
      final CommitSession commitSession = tabletMemory.getCommitSession();
      count[1] = Long.MIN_VALUE;
      try {
        Set<String> absPaths = new HashSet<String>();
        for (FileRef ref : datafiles.keySet())
          absPaths.add(ref.path().toString());

        tabletServer.recover(this.tabletServer.getFileSystem(), this, logEntries, absPaths, new MutationReceiver() {
          @Override
          public void receive(Mutation m) {
            // LogReader.printMutation(m);
            Collection<ColumnUpdate> muts = m.getUpdates();
            for (ColumnUpdate columnUpdate : muts) {
              if (!columnUpdate.hasTimestamp()) {
                // if it is not a user set timestamp, it must have been set
                // by the system
                count[1] = Math.max(count[1], columnUpdate.getTimestamp());
              }
            }
            tabletMemory.mutate(commitSession, Collections.singletonList(m));
            count[0]++;
          }
        });

        if (count[1] != Long.MIN_VALUE) {
          tabletTime.useMaxTimeFromWALog(count[1]);
        }
        commitSession.updateMaxCommittedTime(tabletTime.getTime());

        if (count[0] == 0) {
          log.debug("No replayed mutations applied, removing unused entries for " + extent);
          MetadataTableUtil.removeUnusedWALEntries(extent, logEntries, tabletServer.getLock());
          logEntries.clear();
        }

      } catch (Throwable t) {
        if (acuTableConf.getBoolean(Property.TABLE_FAILURES_IGNORE)) {
          log.warn("Error recovering from log files: ", t);
        } else {
          throw new RuntimeException(t);
        }
      }
      // make some closed references that represent the recovered logs
      currentLogs = new HashSet<DfsLogger>();
      for (LogEntry logEntry : logEntries) {
        for (String log : logEntry.logSet) {
          currentLogs.add(new DfsLogger(tabletServer.getServerConfig(), log, logEntry.getColumnQualifier().toString()));
        }
      }

      log.info("Write-Ahead Log recovery complete for " + this.extent + " (" + count[0] + " mutations applied, " + tabletMemory.getNumEntries()
          + " entries created)");
    }

    String contextName = acuTableConf.get(Property.TABLE_CLASSPATH);
    if (contextName != null && !contextName.equals("")) {
      // initialize context classloader, instead of possibly waiting for it to initialize for a scan
      // TODO this could hang, causing other tablets to fail to load - ACCUMULO-1292
      AccumuloVFSClassLoader.getContextManager().getClassLoader(contextName);
    }

    // do this last after tablet is completely setup because it
    // could cause major compaction to start
    datafileManager = new DatafileManager(datafiles);

    computeNumEntries();

    datafileManager.removeFilesAfterScan(scanFiles);

    // look for hints of a failure on the previous tablet server
    if (!logEntries.isEmpty() || needsMajorCompaction(MajorCompactionReason.NORMAL)) {
      // look for any temp files hanging around
      removeOldTemporaryFiles();
    }

    log.log(TLevel.TABLET_HIST, extent + " opened");
  }
  public String getMeta() {
    if (null == metaReference) {
      throw new IllegalStateException("logger doesn't have meta reference. " + this);
    }
    return metaReference;
  }
  public DfsLogger(ServerResources conf, String filename, String meta) throws IOException {
    this.conf = conf;
    this.logPath = filename;
    metaReference = meta;
  }
  public synchronized void open(String address) throws IOException {
    String filename = UUID.randomUUID().toString();
    String logger = StringUtil.join(Arrays.asList(address.split(":")), "+");

    log.debug("DfsLogger.open() begin");
    VolumeManager fs = conf.getFileSystem();

    logPath = fs.choose(ServerConstants.getWalDirs()) + "/" + logger + "/" + filename;
    metaReference = toString();
    try {
      short replication = (short) conf.getConfiguration().getCount(Property.TSERV_WAL_REPLICATION);
      if (replication == 0)
        replication = fs.getDefaultReplication(new Path(logPath));
      long blockSize = conf.getConfiguration().getMemoryInBytes(Property.TSERV_WAL_BLOCKSIZE);
      if (blockSize == 0)
        blockSize = (long) (conf.getConfiguration().getMemoryInBytes(Property.TSERV_WALOG_MAX_SIZE) * 1.1);
      if (conf.getConfiguration().getBoolean(Property.TSERV_WAL_SYNC))
        logFile = fs.createSyncable(new Path(logPath), 0, replication, blockSize);
      else
        logFile = fs.create(new Path(logPath), true, 0, replication, blockSize);

      String syncMethod = conf.getConfiguration().get(Property.TSERV_WAL_SYNC_METHOD);
      try {
        // hsync: send data to datanodes and sync the data to disk
        sync = logFile.getClass().getMethod(syncMethod);
      } catch (Exception ex) {
        log.warn("Could not find configured " + syncMethod + " method, trying to fall back to old Hadoop sync method", ex);

        try {
          // sync: send data to datanodes
          sync = logFile.getClass().getMethod("sync");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      // Initialize the crypto operations.
      org.apache.accumulo.core.security.crypto.CryptoModule cryptoModule = org.apache.accumulo.core.security.crypto.CryptoModuleFactory.getCryptoModule(conf
          .getConfiguration().get(Property.CRYPTO_MODULE_CLASS));

      // Initialize the log file with a header and the crypto params used to set up this log file.
      logFile.write(LOG_FILE_HEADER_V3.getBytes(Constants.UTF8));

      CryptoModuleParameters params = CryptoModuleFactory.createParamsObjectFromAccumuloConfiguration(conf.getConfiguration());

      NoFlushOutputStream nfos = new NoFlushOutputStream(logFile);
      params.setPlaintextOutputStream(nfos);

      // In order to bootstrap the reading of this file later, we have to record the CryptoModule that was used to encipher it here,
      // so that that crypto module can re-read its own parameters.

      logFile.writeUTF(conf.getConfiguration().get(Property.CRYPTO_MODULE_CLASS));

      params = cryptoModule.getEncryptingOutputStream(params);
      OutputStream encipheringOutputStream = params.getEncryptedOutputStream();

      // If the module just kicks back our original stream, then just use it, don't wrap it in
      // another data OutputStream.
      if (encipheringOutputStream == nfos) {
        log.debug("No enciphering, using raw output stream");
        encryptingLogFile = nfos;
      } else {
        log.debug("Enciphering found, wrapping in DataOutputStream");
        encryptingLogFile = new DataOutputStream(encipheringOutputStream);
      }

      LogFileKey key = new LogFileKey();
      key.event = OPEN;
      key.tserverSession = filename;
      key.filename = filename;
      write(key, EMPTY);
      sync.invoke(logFile);
      log.debug("Got new write-ahead log: " + this);
    } catch (Exception ex) {
      if (logFile != null)
        logFile.close();
      logFile = null;
      encryptingLogFile = null;
      throw new IOException(ex);
    }

    syncThread = new Daemon(new LoggingRunnable(log, new LogSyncingTask()));
    syncThread.setName("Accumulo WALog thread " + toString());
    syncThread.start();
  }
