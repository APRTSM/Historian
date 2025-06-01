  private FileOperations findFileFactory(String file) {
    
    Path p = new Path(file);
    String name = p.getName();
    
    if (name.startsWith(Constants.MAPFILE_EXTENSION + "_")) {
      return new MapFileOperations();
    }
    
    String[] sp = name.split("\\.");
    
    if (sp.length != 2) {
      throw new IllegalArgumentException("File name " + name + " has no extension");
    }
    
    String extension = sp[1];
    
    if (extension.equals(Constants.MAPFILE_EXTENSION) || extension.equals(Constants.MAPFILE_EXTENSION + "_tmp")) {
      return new MapFileOperations();
    } else if (extension.equals(RFile.EXTENSION) || extension.equals(RFile.EXTENSION + "_tmp")) {
      return new RFileOperations();
    } else {
      throw new IllegalArgumentException("File type " + extension + " not supported");
    }
  }
