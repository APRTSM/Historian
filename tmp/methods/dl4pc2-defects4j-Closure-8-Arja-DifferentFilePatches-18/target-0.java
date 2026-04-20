  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      lazyInitInfo();
	info.suppressions = Sets.newHashSet();
    }
    if (info == null) {
		info = new LazilyInitializedInfo();
	}
	info.suppressions.add(suppression);
  }
