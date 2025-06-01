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
  public void create(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    create(tableName, true, TimeType.MILLIS);
  }
