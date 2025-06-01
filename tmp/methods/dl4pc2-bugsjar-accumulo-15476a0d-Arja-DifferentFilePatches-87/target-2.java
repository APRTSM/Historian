  synchronized void addMutation(Mutation m) {
    long now = System.currentTimeMillis();
    for (ColumnUpdate u : m.getUpdates()) {
      Key key = new Key(m.getRow(), 0, m.getRow().length, u.getColumnFamily(), 0, u.getColumnFamily().length, u.getColumnQualifier(), 0,
          u.getColumnQualifier().length, u.getColumnVisibility(), 0, u.getColumnVisibility().length, u.getTimestamp());
      if (u.isDeleted())
        key.setDeleted(true);
      if (!u.hasTimestamp())
        if (timeType.equals(TimeType.LOGICAL))
          key.setTimestamp(mutationCount);
        else
          key.setTimestamp(now);
      
      table.put(new MockMemKey(key, mutationCount), new Value(u.getValue()));
    }
  }
  public void create(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    create(tableName, true, TimeType.MILLIS);
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
