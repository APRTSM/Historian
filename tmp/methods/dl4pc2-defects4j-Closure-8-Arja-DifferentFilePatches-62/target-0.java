  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      if (info == null) {
			info = new LazilyInitializedInfo();
		}
	info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
