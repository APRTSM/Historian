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
