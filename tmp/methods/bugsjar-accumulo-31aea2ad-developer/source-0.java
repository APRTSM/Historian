  public Path getFullPath(FileType fileType, String path) {
    if (path.contains(":"))
      return new Path(path);

    // normalize the path
    Path fullPath = new Path(defaultVolume.getBasePath(), fileType.getDirectory());
    if (path.startsWith("/"))
      path = path.substring(1);
    fullPath = new Path(fullPath, path);

    FileSystem fs = getVolumeByPath(fullPath).getFileSystem();
    return fs.makeQualified(fullPath);
  }
