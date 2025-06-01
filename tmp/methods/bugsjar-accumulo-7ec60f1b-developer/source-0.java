  public void deleteRows(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
    MockTable t = acu.tables.get(tableName);
    Set<Key> keep = new TreeSet<Key>(t.table.tailMap(new Key(start)).headMap(new Key(end)).keySet());
    t.table.keySet().removeAll(keep);
  }
