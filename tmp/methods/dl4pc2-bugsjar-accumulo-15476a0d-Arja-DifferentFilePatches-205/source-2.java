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
  public void create(String tableName, boolean versioningIter, TimeType timeType) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    if (!tableName.matches(Constants.VALID_TABLE_NAME_REGEX)) {
      throw new IllegalArgumentException();
    }
    acu.createTable(username, tableName, versioningIter, timeType);
  }
  public void put(CharSequence columnFamily, CharSequence columnQualifier, CharSequence value) {
    put(columnFamily, columnQualifier, EMPTY_BYTES, false, 0l, false, value);
  }
