  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      lazyInitInfo();
    }
    if (info == null) {
		info = new LazilyInitializedInfo();
	}
  }
