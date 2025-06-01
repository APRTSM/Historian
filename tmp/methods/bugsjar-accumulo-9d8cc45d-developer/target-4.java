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
