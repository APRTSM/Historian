  public void deleteRows(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    if (!exists(tableName))
      throw new TableNotFoundException(tableName, tableName, "");
    MockTable t = acu.tables.get(tableName);
    Text startText = new Text(start);
    Text endText = new Text(end);
    startText.append(ZERO, 0, 1);
    endText.append(ZERO, 0, 1);
    Set<Key> keep = new TreeSet<Key>(t.table.subMap(new Key(startText), new Key(endText)).keySet());
    t.table.keySet().removeAll(keep);
  }
