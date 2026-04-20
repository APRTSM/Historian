  public String toString() {
    if (info == null) {
		info = new LazilyInitializedInfo();
	}
	return "JSDocInfo";
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
