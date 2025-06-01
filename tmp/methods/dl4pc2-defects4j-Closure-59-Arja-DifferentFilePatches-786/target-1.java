    private boolean canBeRedeclared(Node n, Scope s) {
      if (!NodeUtil.isExprAssign(n)) {
        return false;
      }
      Node assign = n.getFirstChild();
      Node lhs = assign.getFirstChild();

      Var var = s.getVar(lhs.getString());
      return var != null &&
          var.getScope() == s && !blacklistedVars.contains(var);
    }
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
