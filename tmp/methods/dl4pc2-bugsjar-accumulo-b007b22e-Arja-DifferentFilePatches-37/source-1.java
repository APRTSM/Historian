  public Long typedReduce(Key key, Iterator<Long> iter) {
    long sum = 0;
    while (iter.hasNext()) {
      sum = safeAdd(sum, iter.next());
    }
    return sum;
  }
  public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive) throws IOException {
    // do not want to seek to the middle of a value that should be combined...
    
    Range seekRange = IteratorUtil.maximizeStartKeyTimeStamp(range);
    
    super.seek(seekRange, columnFamilies, inclusive);
    findTop();
    
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
