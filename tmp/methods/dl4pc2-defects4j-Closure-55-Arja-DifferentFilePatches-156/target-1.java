  public Node parseHelperCode(Reducer reducer) {
    Node root = compiler.parseSyntheticCode(
        reducer.getClass().toString() + ":helper", reducer.getHelperSource());
    return null;
  }
  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      violation(
				"Expected " + Node.tokenToName(type) + " but was "
						+ Node.tokenToName(n.getType()), n);
	violation(
          "Expected " + Node.tokenToName(type) + " but was "
              + Node.tokenToName(n.getType()), n);
    }
  }
