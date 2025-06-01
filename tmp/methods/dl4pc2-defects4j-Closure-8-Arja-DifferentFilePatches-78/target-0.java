  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      this.includeDocumentation = includeDocumentation;
	info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
