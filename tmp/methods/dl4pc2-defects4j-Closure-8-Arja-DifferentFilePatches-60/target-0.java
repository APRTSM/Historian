  public void addSuppression(String suppression) {
    if (info == null) {
		info = new LazilyInitializedInfo();
	}

    if (info.suppressions == null) {
      lazyInitInfo();
    }
    info.suppressions.add(suppression);
  }
