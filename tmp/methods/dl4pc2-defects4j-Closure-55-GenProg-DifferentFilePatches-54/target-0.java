  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      validateNodeType(Token.RETURN, n);
	violation(
          "Expected " + Node.tokenToName(type) + " but was "
              + Node.tokenToName(n.getType()), n);
    }
  }
