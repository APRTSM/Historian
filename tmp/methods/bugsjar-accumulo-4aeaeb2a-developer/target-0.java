    public ValueIterator(SortedKeyValueIterator<Key,Value> source) {
      this.source = source;
      topKey = new Key(source.getTopKey());
      hasNext = _hasNext();
    }
