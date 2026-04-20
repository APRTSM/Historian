  public void addSuppression(String suppression) {
    lazyInitInfo();

    lazyInitInfo();
	if (info.suppressions == null) {
      info.suppressions = Sets.newHashSet();
    }
    if (info == null) {
		info = new LazilyInitializedInfo();
	}
	info.suppressions.add(suppression);
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
