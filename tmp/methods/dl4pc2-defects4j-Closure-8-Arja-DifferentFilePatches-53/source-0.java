    private void blacklistStubVars(NodeTraversal t, Node varNode) {
      for (Node child = varNode.getFirstChild();
           child != null; child = child.getNext()) {
        if (child.getFirstChild() == null) {
          blacklistedVars.add(t.getScope().getVar(child.getString()));
        }
      }
    }
    private boolean canBeRedeclared(Node n, Scope s) {
      if (!NodeUtil.isExprAssign(n)) {
        return false;
      }
      Node assign = n.getFirstChild();
      Node lhs = assign.getFirstChild();

      if (!lhs.isName()) {
        return false;
      }

      Var var = s.getVar(lhs.getString());
      return var != null
          && var.getScope() == s
          && !blacklistedVars.contains(var);
    }
