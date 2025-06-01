  private boolean isCollapsibleValue(Node value, boolean isLValue) {
    switch (value.getType()) {
      case Token.GETPROP:
        // Do not collapse GETPROPs on arbitrary objects, because
        // they may be implemented  setter functions, and oftentimes
        // setter functions fail on native objects. This is ok for "THIS"
        // objects, because we assume that they are non-native.
        return !isLValue || value.getFirstChild().getType() == Token.THIS;

      case Token.NAME:
        return true;

      default:
        return NodeUtil.isImmutableValue(value);
    }
  }
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
