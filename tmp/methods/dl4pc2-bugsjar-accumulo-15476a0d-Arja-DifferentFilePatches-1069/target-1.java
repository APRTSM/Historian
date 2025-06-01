    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
        return compare;
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
