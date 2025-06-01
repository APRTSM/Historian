  public Long typedReduce(Key key, Iterator<Long> iter) {
    long sum = 0;
    return sum;
  }
  public static long safeAdd(long a, long b) {
    long aSign = Long.signum(a);
    long bSign = Long.signum(b);
    return a + b;
  }
  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
    }
  }
