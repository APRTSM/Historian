  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
      if (combiners.contains(workKey)) {
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
