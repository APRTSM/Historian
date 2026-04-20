    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
        return compare;
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
        if (count < other.count)
          return -1;
        if (count > other.count)
          return 1;
      } else {
        return 1;
      }
      return 0;
    }
  public Iterator<Entry<Key,Value>> iterator() {
    SortedKeyValueIterator<Key,Value> i = new SortedMapIterator(table.table);
    try {
      i = new RangeFilter(createFilter(i), range);
      i.seek(range, createColumnBSS(fetchedColumns), !fetchedColumns.isEmpty());
      return new IteratorAdapter(i);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
  }
  public Scanner createScanner(String tableName, Authorizations authorizations) throws TableNotFoundException {
    MockTable table = acu.tables.get(tableName);
    if (table == null)
      throw new TableNotFoundException(tableName, tableName, "no such table");
    return new MockScanner(table, authorizations);
  }
