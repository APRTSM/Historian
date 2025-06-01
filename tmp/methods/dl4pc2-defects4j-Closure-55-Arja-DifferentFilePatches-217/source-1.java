    int estimateSavings() {
      return InlineCostEstimator.getCost(oldChild) -
          InlineCostEstimator.getCost(newChild);
    }
  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      violation(
          "Expected " + Node.tokenToName(type) + " but was "
              + Node.tokenToName(n.getType()), n);
    }
  }
