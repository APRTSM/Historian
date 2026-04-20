  private void findTop() throws IOException {
    // check if aggregation is needed
    if (super.hasTop()) {
      workKey.set(super.getTopKey());
    }
  }
