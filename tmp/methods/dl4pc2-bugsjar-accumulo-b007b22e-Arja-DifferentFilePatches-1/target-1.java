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
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
    }
  }
  public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive) throws IOException {
    // do not want to seek to the middle of a value that should be combined...
    
    Range seekRange = IteratorUtil.maximizeStartKeyTimeStamp(range);
    
    super.seek(seekRange, columnFamilies, inclusive);
    if (range.getStartKey() != null) {
      while (hasTop() && getTopKey().equals(range.getStartKey(), PartialKey.ROW_COLFAM_COLQUAL_COLVIS)
          && getTopKey().getTimestamp() > range.getStartKey().getTimestamp()) {
        // the value has a more recent time stamp, so pass it up
        // log.debug("skipping "+getTopKey());
        next();
      }
      
      while (hasTop() && range.beforeStartKey(getTopKey())) {
        next();
      }
    }
  }
