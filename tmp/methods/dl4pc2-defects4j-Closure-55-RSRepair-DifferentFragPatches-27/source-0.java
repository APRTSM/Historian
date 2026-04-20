    int estimateSavings() {
      return InlineCostEstimator.getCost(oldChild) -
          InlineCostEstimator.getCost(newChild);
    }
