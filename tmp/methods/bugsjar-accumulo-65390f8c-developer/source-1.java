  public Collection<Text> getSplits() {
    return splits;
  }
  public void online(String tableName) throws AccumuloSecurityException, AccumuloException {}
  public void flush(String tableName, Text start, Text end, boolean wait) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    throw new NotImplementedException();
  }
  public void setLocalityGroups(String tableName, Map<String,Set<Text>> groups) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    throw new NotImplementedException();
  }
  public void compact(String tableName, Text start, Text end, boolean flush, boolean wait) throws AccumuloSecurityException, TableNotFoundException,
      AccumuloException {
    throw new NotImplementedException();
  }
  public void merge(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    throw new NotImplementedException();
  }
  public void clearLocatorCache(String tableName) throws TableNotFoundException {
    throw new NotImplementedException();
  }
  public void compact(String tableName, Text start, Text end, List<IteratorSetting> iterators, boolean flush, boolean wait) throws AccumuloSecurityException,
      TableNotFoundException, AccumuloException {
    throw new NotImplementedException();
  }
  public void offline(String tableName) throws AccumuloSecurityException, AccumuloException {
    throw new NotImplementedException();
  }
  public Map<String,Set<Text>> getLocalityGroups(String tableName) throws AccumuloException, TableNotFoundException {
    throw new NotImplementedException();
  }
  public void deleteRows(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    throw new NotImplementedException();
  }
