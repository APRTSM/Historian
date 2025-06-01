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
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    RegExFilter result = new RegExFilter();
    result.setSource(getSource().deepCopy(env));
    result.rowMatcher = copyMatcher(rowMatcher);
    result.colfMatcher = copyMatcher(colfMatcher);
    result.colqMatcher = copyMatcher(colqMatcher);
    result.valueMatcher = copyMatcher(valueMatcher);
    result.orFields = orFields;
    return result;
  }
  private TimestampFilter(SortedKeyValueIterator<Key,Value> iterator, boolean hasStart, long start, boolean startInclusive, boolean hasEnd, long end,
      boolean endInclusive) {
    setSource(iterator);
    this.start = start;
    this.startInclusive = startInclusive;
    this.hasStart = true;
    this.end = end;
    this.endInclusive = endInclusive;
    this.hasEnd = true;
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    return new TimestampFilter(getSource(), hasStart, start, startInclusive, hasEnd, end, endInclusive);
  }
