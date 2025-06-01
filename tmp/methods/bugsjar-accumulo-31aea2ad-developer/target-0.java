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
