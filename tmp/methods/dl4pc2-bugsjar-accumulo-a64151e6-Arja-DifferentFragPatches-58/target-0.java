  public void collect(GarbageCollectionEnvironment gce) throws TableNotFoundException, AccumuloException, AccumuloSecurityException, IOException {

    String lastCandidate = "";

    while (true) {
      List<String> candidates = getCandidates(gce, lastCandidate);

      if (candidates.size() == 0)
        break;
      else
        lastCandidate = candidates.get(candidates.size() - 1);

      long origSize = candidates.size();
      SortedMap<String,String> candidateMap = makeRelative(candidates);

      confirmDeletesTrace(gce, candidateMap);
      gce.incrementInUseStat(origSize - candidateMap.size());

      deleteConfirmed(gce, candidateMap);
    }
  }
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
      for (String token : tokens) {
        if (!token.equals("")) {
          tmp.add(token);
        }
      }
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
    } else if (tokens.length == 3 && (expectedLen == 0 || expectedLen == 3)) {
      relPath = tokens[0] + "/" + tokens[1] + "/" + tokens[2];
    } else if (tokens.length == 2 && (expectedLen == 0 || expectedLen == 2)) {
      relPath = tokens[0] + "/" + tokens[1];
    } else {
      throw new IllegalArgumentException(path);
    }

    return relPath;
  }
