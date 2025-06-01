    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
        return compare;
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
      } else {
        return 1;
      }
      return 0;
    }
  public Scanner createScanner(String tableName, Authorizations authorizations) throws TableNotFoundException {
    MockTable table = acu.tables.get(tableName);
    return new MockScanner(table, authorizations);
  }
