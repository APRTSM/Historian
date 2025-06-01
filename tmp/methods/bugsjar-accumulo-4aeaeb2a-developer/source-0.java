    public ValueIterator(SortedKeyValueIterator<Key,Value> source) {
      this.source = source;
      topKey = source.getTopKey();
      hasNext = _hasNext();
    }
