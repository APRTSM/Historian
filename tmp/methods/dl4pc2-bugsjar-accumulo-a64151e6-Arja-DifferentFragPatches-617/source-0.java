  private SortedMap<String,String> makeRelative(Collection<String> candidates) {

    SortedMap<String,String> ret = new TreeMap<String,String>();

    for (String candidate : candidates) {
      String relPath = makeRelative(candidate, 0);
      ret.put(relPath, candidate);
    }

    return ret;
  }
  private List<String> getCandidates(GarbageCollectionEnvironment gce, String lastCandidate) throws TableNotFoundException, AccumuloException,
      AccumuloSecurityException {
    Span candidatesSpan = Trace.start("getCandidates");
    List<String> candidates;
    try {
      candidates = gce.getCandidates(lastCandidate);
    } finally {
      candidatesSpan.stop();
    }
    return candidates;
  }
