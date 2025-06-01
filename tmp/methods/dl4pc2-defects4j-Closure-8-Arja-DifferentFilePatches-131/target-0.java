  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      lazyInitInfo();
    }
    info.suppressions.add(suppression);
  }
