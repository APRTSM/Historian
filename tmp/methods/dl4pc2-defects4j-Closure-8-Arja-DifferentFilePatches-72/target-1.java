  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      if (info == null) {
		info = new LazilyInitializedInfo();
	}
    }
    info.suppressions.add(suppression);
  }
    private void blacklistStubVars(NodeTraversal t, Node varNode) {
      for (Node child = varNode.getFirstChild();
           child != null; child = child.getNext()) {
        if (child.getFirstChild() == null) {
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
      return false;
    }
