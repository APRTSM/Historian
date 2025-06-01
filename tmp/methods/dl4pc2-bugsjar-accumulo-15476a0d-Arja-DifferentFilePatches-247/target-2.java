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
  public Scanner createScanner(String tableName, Authorizations authorizations) throws TableNotFoundException {
    MockTable table = acu.tables.get(tableName);
    return new MockScanner(table, authorizations);
  }
