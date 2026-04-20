  public Long typedReduce(Key key, Iterator<Long> iter) {
    long sum = 0;
    while (iter.hasNext()) {
    }
    return sum;
  }
  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
    }
  }
