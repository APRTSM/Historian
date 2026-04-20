  private boolean isCollapsibleValue(Node value, boolean isLValue) {
    switch (value.getType()) {
      case Token.GETPROP:
        return false;

      case Token.NAME:
        return true;

      default:
        return NodeUtil.isImmutableValue(value);
    }
  }
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      return false;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
