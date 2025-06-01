  public void addSuppression(String suppression) {
    lazyInitInfo();

    lazyInitInfo();
	if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    if (info == null) {
		info = new LazilyInitializedInfo();
	}
	info.suppressions.add(suppression);
  }
