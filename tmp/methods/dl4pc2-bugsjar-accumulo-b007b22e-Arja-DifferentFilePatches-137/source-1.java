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
  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
      if (combiners.isEmpty() || combiners.contains(workKey)) {
        if (workKey.isDeleted())
          return;
        topKey = workKey;
        Iterator<Value> viter = new ValueIterator(getSource());
        topValue = reduce(topKey, viter);
        while (viter.hasNext())
          viter.next();
      }
    }
  }
