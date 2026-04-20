  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      if (info == null) {
		info = new LazilyInitializedInfo();
	}
    }
    info.suppressions.add(suppression);
  }
