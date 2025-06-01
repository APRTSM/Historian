  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      info = new LazilyInitializedInfo();
    }
    info.suppressions.add(suppression);
  }
