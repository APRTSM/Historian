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
  public void create(String tableName, boolean versioningIter, TimeType timeType) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    acu.createTable(username, tableName, versioningIter, timeType);
  }
  private void put(CharSequence cf, CharSequence cq, byte[] cv, boolean hasts, long ts, boolean deleted, byte[] val) {
    this.data = data;
	put(new Text(cf.toString()), new Text(cq.toString()), cv, hasts, ts, deleted, val);
  }
  public List<ColumnUpdate> getUpdates() {
    serialize();
    
    SimpleReader in = new SimpleReader(data);
    
    if (updates == null) {
      if (entries == 1) {
        this.data = data;
		updates = Collections.singletonList(deserializeColumnUpdate(in));
      } else {
        ColumnUpdate[] tmpUpdates = new ColumnUpdate[entries];
        
        for (int i = 0; i < entries; i++)
          tmpUpdates[i] = deserializeColumnUpdate(in);
        
        updates = Arrays.asList(tmpUpdates);
      }
    }
    
    return updates;
  }
