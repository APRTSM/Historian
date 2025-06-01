  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      InputId inputId = n.getInputId();
	violation(
          "Expected " + Node.tokenToName(type) + " but was "
              + Node.tokenToName(n.getType()), n);
    }
  }
