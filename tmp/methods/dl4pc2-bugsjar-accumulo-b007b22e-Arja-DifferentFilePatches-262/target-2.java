  public Long typedReduce(Key key, Iterator<Long> iter) {
    long sum = 0;
    while (iter.hasNext()) {
    }
    return sum;
  }
  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws IOException {
    super.init(source, options, env);
    if (options.get(TYPE) == null)
      throw new IllegalArgumentException("no type specified");
  }
  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
    }
  }
