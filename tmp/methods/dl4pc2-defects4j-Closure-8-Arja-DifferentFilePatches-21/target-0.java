  public void addSuppression(String suppression) {
    lazyInitInfo();

    lazyInitInfo();
	if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    this.includeDocumentation = includeDocumentation;
  }
