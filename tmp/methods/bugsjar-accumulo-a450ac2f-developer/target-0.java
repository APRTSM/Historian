    public RangesFilter deepCopy(IteratorEnvironment env) {
      return new RangesFilter(getSource().deepCopy(env), ranges);
    }
  public Iterator<Entry<Key,Value>> iterator() {
    if (ranges == null) {
      throw new IllegalStateException("ranges not set");
    }

    IteratorChain chain = new IteratorChain();
    for (Range range : ranges) {
      SortedKeyValueIterator<Key,Value> i = new RangesFilter(new SortedMapIterator(table.table), ranges);
      try {
        i = createFilter(i);
        i.seek(range, createColumnBSS(fetchedColumns), !fetchedColumns.isEmpty());
        chain.addIterator(new IteratorAdapter(i));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return chain;
  }
    public RangesFilter(SortedKeyValueIterator<Key,Value> iterator, List<Range> ranges) {
      setSource(iterator);
      this.ranges = ranges;
    }
