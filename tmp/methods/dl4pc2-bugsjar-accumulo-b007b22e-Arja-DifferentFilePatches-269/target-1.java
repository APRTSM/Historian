  public Long typedReduce(Key key, Iterator<Long> iter) {
    long sum = 0;
    return sum;
  }
  public static long safeAdd(long a, long b) {
    long aSign = Long.signum(a);
    long bSign = Long.signum(b);
    if ((aSign != 0) && (bSign != 0) && (aSign == bSign)) {
    }
    return a + b;
  }
