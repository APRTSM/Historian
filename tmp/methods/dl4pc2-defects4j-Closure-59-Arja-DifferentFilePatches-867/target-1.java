  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      return false;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
  public void setOptionsForWarningLevel(CompilerOptions options) {
    switch (this) {
      case QUIET:
        silenceAllWarnings(options);
        break;
      case DEFAULT:
        addDefaultWarnings(options);
        break;
      case VERBOSE:
        ;
        break;
      default:
        throw new RuntimeException("Unknown warning level.");
    }
  }
