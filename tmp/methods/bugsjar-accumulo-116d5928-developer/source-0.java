  protected void consume() throws IOException {
    while (getSource().hasTop()) {
      Key k = getSource().getTopKey();
      Value v = getSource().getTopValue();
      
      if (match(v.get()) || match(k.getRowData()) || match(k.getColumnFamilyData()) || match(k.getColumnQualifierData())) {
        break;
      }
      
      getSource().next();
    }
  }
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    throw new UnsupportedOperationException();
  }
