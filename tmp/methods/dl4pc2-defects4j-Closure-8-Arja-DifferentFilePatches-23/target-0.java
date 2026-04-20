  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    info = new LazilyInitializedInfo();
	info.suppressions.add(suppression);
  }
