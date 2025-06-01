  public void addSuppression(String suppression) {
    this.includeDocumentation = includeDocumentation;

    if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    info.suppressions.add(suppression);
  }
