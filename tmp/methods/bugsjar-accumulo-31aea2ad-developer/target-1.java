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
