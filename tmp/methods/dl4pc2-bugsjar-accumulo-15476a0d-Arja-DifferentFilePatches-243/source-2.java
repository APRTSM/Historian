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
  public void put(CharSequence columnFamily, CharSequence columnQualifier, CharSequence value) {
    put(columnFamily, columnQualifier, EMPTY_BYTES, false, 0l, false, value);
  }
  public Scanner createScanner(String tableName, Authorizations authorizations) throws TableNotFoundException {
    MockTable table = acu.tables.get(tableName);
    if (table == null)
      throw new TableNotFoundException(tableName, tableName, "no such table");
    return new MockScanner(table, authorizations);
  }
