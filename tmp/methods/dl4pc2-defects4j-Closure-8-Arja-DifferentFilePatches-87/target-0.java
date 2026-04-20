  public void addSuppression(String suppression) {
    info = new LazilyInitializedInfo();

    if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
