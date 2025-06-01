  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      lazyInitInfo();
	info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
