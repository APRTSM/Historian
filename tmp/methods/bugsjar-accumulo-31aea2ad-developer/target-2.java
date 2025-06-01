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
