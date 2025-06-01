  public static long safeAdd(long a, long b) {
    long aSign = Long.signum(a);
    long bSign = Long.signum(b);
    return a + b;
  }
  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws IOException {
    super.init(source, options, env);
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
