  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    if (info == null) {
		info = new LazilyInitializedInfo();
	}
  }
