  public void setLocalityGroups(Map<String,Set<Text>> groups) {
    localityGroups = groups;
  }
  public Map<String,Set<Text>> getLocalityGroups() {
    return localityGroups;
  }
  public void flush(String tableName, Text start, Text end, boolean wait) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
 }
  public Map<String,Set<Text>> getLocalityGroups(String tableName) throws AccumuloException, TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
    return acu.tables.get(tableName).getLocalityGroups();
  }
  public void deleteRows(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
    MockTable t = acu.tables.get(tableName);
    Set<Key> keep = new TreeSet<Key>(t.table.tailMap(new Key(start)).headMap(new Key(end)).keySet());
    t.table.keySet().removeAll(keep);
  }
  public void offline(String tableName) throws AccumuloSecurityException, AccumuloException {
    if (!exists(tableName))
      throw new AccumuloException(tableName + " does not exists");
  }
  public void compact(String tableName, Text start, Text end, List<IteratorSetting> iterators, boolean flush, boolean wait) throws AccumuloSecurityException,
      TableNotFoundException, AccumuloException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
  }
  public void setLocalityGroups(String tableName, Map<String,Set<Text>> groups) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
    acu.tables.get(tableName).setLocalityGroups(groups);
  }
  public void online(String tableName) throws AccumuloSecurityException, AccumuloException {
    if (!exists(tableName))
      throw new AccumuloException(tableName + " does not exists");
  }
  public Set<Range> splitRangeByTablets(String tableName, Range range, int maxSplits) throws AccumuloException, AccumuloSecurityException,
      TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
    return Collections.singleton(range);
  }
  public void clearLocatorCache(String tableName) throws TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
  }
  public void merge(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
}
  public void compact(String tableName, Text start, Text end, boolean flush, boolean wait) throws AccumuloSecurityException, TableNotFoundException,
      AccumuloException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
  }
