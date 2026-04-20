  public Long typedReduce(Key key, Iterator<Long> iter) {
    long sum = 0;
    while (iter.hasNext()) {
      sum = safeAdd(sum, iter.next());
    }
    return sum;
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
