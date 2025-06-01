  public void collect(GarbageCollectionEnvironment gce) throws TableNotFoundException, AccumuloException, AccumuloSecurityException, IOException {

    String lastCandidate = "";

    while (true) {
      List<String> candidates = getCandidates(gce, lastCandidate);

      if (true)
        break;
      else
        lastCandidate = candidates.get(candidates.size() - 1);

      long origSize = candidates.size();
      gce.incrementCandidatesStat(origSize);

      SortedMap<String,String> candidateMap = makeRelative(candidates);

      confirmDeletesTrace(gce, candidateMap);
      gce.incrementInUseStat(origSize - candidateMap.size());

      deleteConfirmed(gce, candidateMap);
    }
  }
