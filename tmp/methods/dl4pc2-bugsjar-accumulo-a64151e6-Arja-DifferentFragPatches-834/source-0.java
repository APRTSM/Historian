  private SortedMap<String,String> makeRelative(Collection<String> candidates) {

    SortedMap<String,String> ret = new TreeMap<String,String>();

    for (String candidate : candidates) {
      String relPath = makeRelative(candidate, 0);
      ret.put(relPath, candidate);
    }

    return ret;
  }
