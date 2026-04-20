  protected void ensureSyncIsEnabled() {
    for (Entry<String,Volume> entry : getFileSystems().entrySet()) {
      final String volumeName = entry.getKey();
      FileSystem fs = entry.getValue().getFileSystem();

      if (ViewFSUtils.isViewFS(fs)) {
        try {
          FileSystem resolvedFs = ViewFSUtils.resolvePath(fs, new Path("/")).getFileSystem(fs.getConf());
          log.debug("resolved " + fs.getUri() + " to " + resolvedFs.getUri() + " for sync check");
          fs = resolvedFs;
        } catch (IOException e) {
          log.warn("Failed to resolve " + fs.getUri(), e);
        }
      }

      if (fs instanceof DistributedFileSystem) {
        final String DFS_DURABLE_SYNC = "dfs.durable.sync", DFS_SUPPORT_APPEND = "dfs.support.append";
        final String ticketMessage = "See ACCUMULO-623 and ACCUMULO-1637 for more details.";
        // Check to make sure that we have proper defaults configured
        try {
          // If the default is off (0.20.205.x or 1.0.x)
          DFSConfigKeys configKeys = new DFSConfigKeys();

          // Can't use the final constant itself as Java will inline it at compile time
          Field dfsSupportAppendDefaultField = configKeys.getClass().getField("DFS_SUPPORT_APPEND_DEFAULT");
          boolean dfsSupportAppendDefaultValue = dfsSupportAppendDefaultField.getBoolean(configKeys);

          if (!dfsSupportAppendDefaultValue) {
            // See if the user did the correct override
            if (!fs.getConf().getBoolean(DFS_SUPPORT_APPEND, false)) {
              String msg = "Accumulo requires that dfs.support.append to true. " + ticketMessage;
              log.fatal(msg);
              throw new RuntimeException(msg);
            }
          }
        } catch (NoSuchFieldException e) {
          // If we can't find DFSConfigKeys.DFS_SUPPORT_APPEND_DEFAULT, the user is running
          // 1.1.x or 1.2.x. This is ok, though, as, by default, these versions have append/sync enabled.
        } catch (Exception e) {
          log.warn("Error while checking for " + DFS_SUPPORT_APPEND + " on volume " + volumeName
              + ". The user should ensure that Hadoop is configured to properly supports append and sync. " + ticketMessage, e);
        }

        // If either of these parameters are configured to be false, fail.
        // This is a sign that someone is writing bad configuration.
        if (!fs.getConf().getBoolean(DFS_SUPPORT_APPEND, true) || !fs.getConf().getBoolean(DFS_DURABLE_SYNC, true)) {
          String msg = "Accumulo requires that " + DFS_SUPPORT_APPEND + " and " + DFS_DURABLE_SYNC + " not be configured as false. " + ticketMessage;
          log.fatal(msg);
          throw new RuntimeException(msg);
        }

        try {
          // Check DFSConfigKeys to see if DFS_DATANODE_SYNCONCLOSE_KEY exists (should be everything >=1.1.1 and the 0.23 line)
          Class<?> dfsConfigKeysClz = Class.forName("org.apache.hadoop.hdfs.DFSConfigKeys");
          dfsConfigKeysClz.getDeclaredField("DFS_DATANODE_SYNCONCLOSE_KEY");

          // Everything else
          if (!fs.getConf().getBoolean("dfs.datanode.synconclose", false)) {
            log.warn("dfs.datanode.synconclose set to false in hdfs-site.xml: data loss is possible on system reset or power loss");
          }
        } catch (ClassNotFoundException ex) {
          // hadoop 1.0.X or hadoop 1.1.0
        } catch (SecurityException e) {
          // hadoop 1.0.X or hadoop 1.1.0
        } catch (NoSuchFieldException e) {
          // hadoop 1.0.X or hadoop 1.1.0
        }
      }
    }
  }
  public static VolumeManager get(AccumuloConfiguration conf) throws IOException {
    final Map<String,Volume> volumes = new HashMap<String,Volume>();
    final Configuration hadoopConf = CachedConfiguration.getInstance();

    // The "default" Volume for Accumulo (in case no volumes are specified)
    for (String volumeUriOrDir : VolumeConfiguration.getVolumeUris(conf)) {
      if (volumeUriOrDir.equals(DEFAULT))
        // Cannot re-define the default volume
        throw new IllegalArgumentException();

      // We require a URI here, fail if it doesn't look like one
      if (volumeUriOrDir.contains(":")) {
        volumes.put(volumeUriOrDir, VolumeConfiguration.create(new Path(volumeUriOrDir), hadoopConf));
      } else {
        throw new IllegalArgumentException("Expected fully qualified URI for " + Property.INSTANCE_VOLUMES.getKey() + " got " + volumeUriOrDir);
      }
    }

    return new VolumeManagerImpl(volumes, VolumeConfiguration.getDefaultVolume(hadoopConf, conf), conf);
  }
  public boolean isReady() throws IOException {
    for (Volume volume : getFileSystems().values()) {
      FileSystem fs = volume.getFileSystem();

      if (ViewFSUtils.isViewFS(fs)) {
        try {
          FileSystem resolvedFs = ViewFSUtils.resolvePath(fs, new Path("/")).getFileSystem(fs.getConf());
          log.debug("resolved " + fs.getUri() + " to " + resolvedFs.getUri() + " for ready check");
          fs = resolvedFs;
        } catch (IOException e) {
          log.warn("Failed to resolve " + fs.getUri(), e);
        }
      }

      if (!(fs instanceof DistributedFileSystem))
        continue;
      DistributedFileSystem dfs = (DistributedFileSystem) fs;
      // So this: if (!dfs.setSafeMode(SafeModeAction.SAFEMODE_GET))
      // Becomes this:
      Class<?> safeModeAction;
      try {
        // hadoop 2.0
        safeModeAction = Class.forName("org.apache.hadoop.hdfs.protocol.HdfsConstants$SafeModeAction");
      } catch (ClassNotFoundException ex) {
        // hadoop 1.0
        try {
          safeModeAction = Class.forName("org.apache.hadoop.hdfs.protocol.FSConstants$SafeModeAction");
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Cannot figure out the right class for Constants");
        }
      }
      Object get = null;
      for (Object obj : safeModeAction.getEnumConstants()) {
        if (obj.toString().equals("SAFEMODE_GET"))
          get = obj;
      }
      if (get == null) {
        throw new RuntimeException("cannot find SAFEMODE_GET");
      }
      try {
        Method setSafeMode = dfs.getClass().getMethod("setSafeMode", safeModeAction);
        boolean inSafeMode = (Boolean) setSafeMode.invoke(dfs, get);
        if (inSafeMode) {
          return false;
        }
      } catch (IllegalArgumentException exception) {
        /* Send IAEs back as-is, so that those that wrap UnknownHostException can be handled in the same place as similar sources of failure. */
        throw exception;
      } catch (Exception ex) {
        throw new RuntimeException("cannot find method setSafeMode");
      }
    }
    return true;
  }
