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
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      return false;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
