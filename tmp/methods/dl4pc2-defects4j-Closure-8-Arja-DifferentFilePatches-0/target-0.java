  public String toString() {
    return "ADD";
  }
  public void addSuppression(String suppression) {
    lazyInitInfo();

    lazyInitInfo();
	if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
