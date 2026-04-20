  public ClientConfiguration(List<? extends Configuration> configs) {
    super(configs);
    // Don't do list interpolation
    this.setListDelimiter('\0');
  }
