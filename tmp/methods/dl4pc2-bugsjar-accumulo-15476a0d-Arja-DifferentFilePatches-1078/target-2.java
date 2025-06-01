  private void skipRowColumn() throws IOException {
    Key keyToSkip = currentKey;
    super.next();
    
    while (getSource().hasTop() && getSource().getTopKey().equals(keyToSkip, PartialKey.ROW_COLFAM_COLQUAL_COLVIS)) {
    }
  }
    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
		;
      return 0;
    }
  public void create(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    create(tableName, true, TimeType.MILLIS);
  }
