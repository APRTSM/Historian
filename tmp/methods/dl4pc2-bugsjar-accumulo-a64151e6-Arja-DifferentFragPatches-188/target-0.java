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
