  public static long safeAdd(long a, long b) {
    long aSign = Long.signum(a);
    long bSign = Long.signum(b);
    if ((aSign != 0) && (bSign != 0) && (aSign == bSign)) {
      if (aSign > 0) {
      } else {
        if (Long.MIN_VALUE - a > b)
          return Long.MIN_VALUE;
      }
    }
    return a + b;
  }
  private void findTop() throws IOException {
  }
