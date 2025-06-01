  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    Combiner newInstance;
    try {
      newInstance = this.getClass().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    newInstance.setSource(getSource().deepCopy(env));
    newInstance.combiners = combiners;
    newInstance.combineAllColumns = combineAllColumns;
    return newInstance;
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    TypedValueCombiner<V> newInstance = (TypedValueCombiner<V>) super.deepCopy(env);
    newInstance.setEncoder(encoder);
    return newInstance;
  }
  public VersioningIterator(SortedKeyValueIterator<Key,Value> iterator, int maxVersions) {
    super();
    this.setSource(iterator);
    this.maxVersions = maxVersions;
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    AgeOffFilter copy = (AgeOffFilter) super.deepCopy(env);
    copy.currentTime = currentTime;
    copy.threshold = threshold;
    return copy;
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    ColumnAgeOffFilter copy = (ColumnAgeOffFilter) super.deepCopy(env);
    copy.currentTime = currentTime;
    copy.ttls = ttls;
    return copy;
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    RegExFilter result = (RegExFilter) super.deepCopy(env);
    result.rowMatcher = copyMatcher(rowMatcher);
    result.colfMatcher = copyMatcher(colfMatcher);
    result.colqMatcher = copyMatcher(colqMatcher);
    result.valueMatcher = copyMatcher(valueMatcher);
    result.orFields = orFields;
    return result;
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    TimestampFilter copy = (TimestampFilter) super.deepCopy(env);
    copy.hasStart = hasStart;
    copy.start = start;
    copy.startInclusive = startInclusive;
    copy.hasEnd = hasEnd;
    copy.end = end;
    copy.endInclusive = endInclusive;
    return copy;
  }
  public VersioningIterator deepCopy(IteratorEnvironment env) {
    VersioningIterator copy = new VersioningIterator();
    copy.setSource(getSource().deepCopy(env));
    copy.maxVersions = maxVersions;
    return copy;
  }
