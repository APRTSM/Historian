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
  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        if (diff != null) {
			return "Node tree inequality:" + "\nTree1:\n" + toStringTree()
					+ "\n\nTree2:\n" + node2.toStringTree() + "\n\nSubtree1: "
					+ diff.nodeA.toStringTree() + "\n\nSubtree2: "
					+ diff.nodeB.toStringTree();
		}
      }
      return null;
  }
