  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
      if (combiners.isEmpty() || combiners.contains(workKey)) {
        if (workKey.isDeleted())
          return;
        Iterator<Value> viter = new ValueIterator(getSource());
      }
    }
  }
