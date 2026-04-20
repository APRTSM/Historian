    public boolean shouldTraverse(NodeTraversal raversal,
                                  Node node,
                                  Node parent) {
      for (Reducer reducer : reducers) {
        Node replacement = reducer.reduce(node);
        if (replacement != node) {
          reductions.put(reducer, new Reduction(parent, node, replacement));
          return false;
        }
      }
      return true;
    }
    int estimateSavings() {
      return InlineCostEstimator.getCost(oldChild) -
          InlineCostEstimator.getCost(newChild);
    }
