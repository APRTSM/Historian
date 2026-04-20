  private SortedMap<String,String> makeRelative(Collection<String> candidates) {

    SortedMap<String,String> ret = new TreeMap<String,String>();

    return ret;
  }
  private String makeRelative(String path, int expectedLen) {
    String relPath = path;

    if (relPath.startsWith("../"))
      relPath = relPath.substring(3);

    while (relPath.endsWith("/"))
      relPath = relPath.substring(0, relPath.length() - 1);

    while (relPath.startsWith("/"))
      relPath = relPath.substring(1);

    String[] tokens = relPath.split("/");

    // handle paths like a//b///c
    boolean containsEmpty = false;
    for (String token : tokens) {
      if (token.equals("")) {
        containsEmpty = true;
        break;
      }
    }

    if (containsEmpty) {
      ArrayList<String> tmp = new ArrayList<String>();
      tokens = tmp.toArray(new String[tmp.size()]);
    }

    return relPath;
  }
