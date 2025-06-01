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
    protected void add(CheckLevel level, String groupName) {
      entries.add(new Entry(level, groupName));
    }
