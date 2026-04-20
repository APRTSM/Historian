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
