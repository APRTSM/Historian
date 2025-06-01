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

    if (tokens.length > 3) {
      if (!path.contains(":"))
        throw new IllegalArgumentException(path);

      if (tokens[tokens.length - 4].equals(ServerConstants.TABLE_DIR) && (expectedLen == 0 || expectedLen == 3)) {
        relPath = tokens[tokens.length - 3] + "/" + tokens[tokens.length - 2] + "/" + tokens[tokens.length - 1];
      } else if (tokens[tokens.length - 3].equals(ServerConstants.TABLE_DIR) && (expectedLen == 0 || expectedLen == 2)) {
        relPath = tokens[tokens.length - 2] + "/" + tokens[tokens.length - 1];
      } else {
        throw new IllegalArgumentException(path);
      }
    } else
		;

    return relPath;
  }
