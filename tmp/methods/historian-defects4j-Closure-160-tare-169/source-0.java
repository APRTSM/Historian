  public CheckLevel level(JSError error) {
    final String errorPath = error.sourceName;
    if (error.level != CheckLevel.ERROR && errorPath != null) {
      boolean inPath = false;
      for (String path : paths) {
        inPath |= errorPath.contains(path);
      }
      if (inPath ^ (showType == ShowType.INCLUDE)) {
        return CheckLevel.OFF;
      }
    }
    return null;
  }
