  public VersioningIterator(SortedKeyValueIterator<Key,Value> iterator, int maxVersions) {
    super(iterator, maxVersions);
  }
  public AgeOffFilter() {}
  private AgeOffFilter(SortedKeyValueIterator<Key,Value> iterator, long threshold, long currentTime) {
    setSource(iterator);
    this.threshold = threshold;
    this.currentTime = currentTime;
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    return new AgeOffFilter(getSource(), threshold, currentTime);
  }
  public ColumnAgeOffFilter() {}
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    return new ColumnAgeOffFilter(getSource(), ttls, currentTime);
  }
  private ColumnAgeOffFilter(SortedKeyValueIterator<Key,Value> iterator, TTLSet ttls, long currentTime) {
    setSource(iterator);
    this.ttls = ttls;
    this.currentTime = currentTime;
  }
