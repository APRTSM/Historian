  public Iterator<Entry<Key,Value>> iterator() {
    if (ranges == null) {
      throw new IllegalStateException("ranges not set");
    }

    IteratorChain chain = new IteratorChain();
    for (Range range : ranges) {
      SortedKeyValueIterator<Key,Value> i = new SortedMapIterator(table.table);
      try {
        i = new RangesFilter(createFilter(i), ranges);
        i.seek(range, createColumnBSS(fetchedColumns), !fetchedColumns.isEmpty());
        chain.addIterator(new IteratorAdapter(i));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return chain;
  }
    RangesFilter(SortedKeyValueIterator<Key,Value> iterator, List<Range> ranges) {
      setSource(iterator);
      this.ranges = ranges;
    }
