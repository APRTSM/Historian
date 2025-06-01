  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info == null) {
		info = new LazilyInitializedInfo();
	}
	if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
