  public Node parseHelperCode(Reducer reducer) {
    Node root = compiler.parseSyntheticCode(
        reducer.getClass().toString() + ":helper", reducer.getHelperSource());
    return (root != null) ? root.removeFirstChild() : null;
  }
    int estimateSavings() {
      return InlineCostEstimator.getCost(oldChild) -
          InlineCostEstimator.getCost(newChild);
    }
