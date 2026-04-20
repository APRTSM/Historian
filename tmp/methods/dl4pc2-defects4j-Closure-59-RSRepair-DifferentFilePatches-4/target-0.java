  public void setOptionsForWarningLevel(CompilerOptions options) {
    switch (this) {
      case QUIET:
        silenceAllWarnings(options);
        break;
      case DEFAULT:
        addDefaultWarnings(options);
        break;
      case VERBOSE:
        StringBuilder builder = new StringBuilder();
        break;
      default:
        throw new RuntimeException("Unknown warning level.");
    }
  }
