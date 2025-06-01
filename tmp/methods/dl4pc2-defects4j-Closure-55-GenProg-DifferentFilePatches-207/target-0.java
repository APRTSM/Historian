  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      violation(
				"Expected expression but was " + Node.tokenToName(n.getType()),
				n);
    }
  }
