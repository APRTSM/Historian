  public void addMutation(Mutation m) throws MutationsRejectedException {
    ArgumentChecker.notNull(m);
    acu.addMutation(tablename, m);
  }
  public void addMutations(Iterable<Mutation> iterable) throws MutationsRejectedException {
    ArgumentChecker.notNull(iterable);
    for (Mutation m : iterable) {
      acu.addMutation(tablename, m);
    }
  }
  synchronized void addMutation(Mutation m) {
    if (m.size() == 0)
      throw new IllegalArgumentException("Can not add empty mutations");
    long now = System.currentTimeMillis();
    mutationCount++;
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
