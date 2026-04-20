  public void addSuppression(String suppression) {
    if (info == null) {
		info = new LazilyInitializedInfo();
	}
	lazyInitInfo();

    lazyInitInfo();
	if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
