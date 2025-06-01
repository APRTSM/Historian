  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    lazyInitInfo();
	info.suppressions.add(suppression);
  }
