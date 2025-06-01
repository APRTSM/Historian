  public Long typedReduce(Key key, Iterator<Long> iter) {
    long sum = 0;
    while (iter.hasNext()) {
      sum = safeAdd(sum, iter.next());
    }
    return sum;
  }
  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws IOException {
    super.init(source, options, env);
    if (options.get(TYPE) == null)
      throw new IllegalArgumentException("no type specified");
    switch (Type.valueOf(options.get(TYPE))) {
      case VARNUM:
        encoder = new VarNumEncoder();
        return;
      case LONG:
        encoder = new LongEncoder();
        return;
      case STRING:
        encoder = new StringEncoder();
        return;
      default:
        throw new IllegalArgumentException();
    }
  }
