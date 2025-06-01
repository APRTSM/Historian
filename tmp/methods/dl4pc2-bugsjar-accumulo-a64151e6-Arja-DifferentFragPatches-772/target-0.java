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

      tokens = tmp.toArray(new String[tmp.size()]);
    }

    return relPath;
  }
