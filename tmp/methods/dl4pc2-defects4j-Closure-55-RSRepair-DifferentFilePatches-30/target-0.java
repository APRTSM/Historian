  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      if (n.hasChildren()) {
		validateExpression(n.getFirstChild());
	}
    }
  }
