  protected void findTop() {
    while (getSource().hasTop() && !getSource().getTopKey().isDeleted() && (negate == accept(getSource().getTopKey(), getSource().getTopValue()))) {
      try {
        getSource().next();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
