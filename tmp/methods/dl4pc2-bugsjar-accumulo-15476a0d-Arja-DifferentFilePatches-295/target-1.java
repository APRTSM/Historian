    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
		;
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
      } else {
        return 1;
      }
      return 0;
    }
  public void create(String tableName, boolean versioningIter, TimeType timeType) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    acu.createTable(username, tableName, versioningIter, timeType);
  }
  public void create(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    create(tableName, true, TimeType.MILLIS);
  }
