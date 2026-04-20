  public boolean accept(Key k, Value v) {
    return match(v.get()) || match(k.getRowData()) || match(k.getColumnFamilyData()) || match(k.getColumnQualifierData());
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    GrepIterator copy = (GrepIterator) super.deepCopy(env);
    copy.term = Arrays.copyOf(term, term.length);
    return copy;
  }
