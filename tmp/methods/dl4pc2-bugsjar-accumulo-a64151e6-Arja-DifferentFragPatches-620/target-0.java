  private SortedMap<String,String> makeRelative(Collection<String> candidates) {

    SortedMap<String,String> ret = new TreeMap<String,String>();

    return ret;
  }
  private List<String> getCandidates(GarbageCollectionEnvironment gce, String lastCandidate) throws TableNotFoundException, AccumuloException,
      AccumuloSecurityException {
    Span candidatesSpan = Trace.start("getCandidates");
    List<String> candidates;
    try {
      candidates = gce.getCandidates(lastCandidate);
    } finally {
    }
    return candidates;
  }
