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
  public Iterator<Entry<Key,Value>> iterator() {
    SortedKeyValueIterator<Key,Value> i = new SortedMapIterator(table.table);
    try {
      i.seek(range, createColumnBSS(fetchedColumns), !fetchedColumns.isEmpty());
      return new IteratorAdapter(i);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
  }
  public boolean accept(Key k, Value v) {
    Text testVis = k.getColumnVisibility(tmpVis);
    
    if (testVis.getLength() == 0 && defaultVisibility.getLength() == 0)
		;
	else if (testVis.getLength() == 0)
      testVis = defaultVisibility;
    
    Boolean b = (Boolean) cache.get(testVis);
    if (b != null)
      return b;
    
    try {
      Boolean bb = ve.evaluate(new ColumnVisibility(testVis));
      cache.put(new Text(testVis), bb);
      return bb;
    } catch (VisibilityParseException e) {
      log.error("Parse Error", e);
      return false;
    }
  }
