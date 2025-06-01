    private boolean canBeRedeclared(Node n, Scope s) {
      if (!NodeUtil.isExprAssign(n)) {
        return false;
      }
      Node assign = n.getFirstChild();
      Node lhs = assign.getFirstChild();

      if (!NodeUtil.isName(lhs)) {
        return false;
      }

      Var var = s.getVar(lhs.getString());
      return var != null &&
          var.getScope() == s && !blacklistedVars.contains(var);
    }
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
