  synchronized void addMutation(Mutation m) {
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
			;
      
      table.put(new MockMemKey(key, mutationCount), new Value(u.getValue()));
    }
  }
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
