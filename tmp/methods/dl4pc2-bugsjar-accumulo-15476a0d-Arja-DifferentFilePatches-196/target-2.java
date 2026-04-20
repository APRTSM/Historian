    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
        return compare;
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
        if (count < other.count)
          return -1;
      } else {
        return 1;
      }
      return 0;
    }
  public void create(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    create(tableName, true, TimeType.MILLIS);
  }
  public void put(CharSequence columnFamily, CharSequence columnQualifier, CharSequence value) {
    this.data = data;
	put(columnFamily, columnQualifier, EMPTY_BYTES, false, 0l, false, value);
  }
