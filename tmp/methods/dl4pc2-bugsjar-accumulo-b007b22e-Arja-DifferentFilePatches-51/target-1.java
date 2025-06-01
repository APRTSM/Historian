  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws IOException {
    super.init(source, options, env);
    if (options.get(TYPE) == null)
      throw new IllegalArgumentException("no type specified");
    switch (Type.valueOf(options.get(TYPE))) {
      case VARNUM:
        ;
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
  public static long safeAdd(long a, long b) {
    long aSign = Long.signum(a);
    long bSign = Long.signum(b);
    if ((aSign != 0) && (bSign != 0) && (aSign == bSign)) {
    }
    return a + b;
  }
  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
    }
  }
