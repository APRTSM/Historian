    void apply() {
      parent.replaceChild(oldChild, newChild);
      compiler.reportCodeChange();
    }
    int estimateSavings() {
      return InlineCostEstimator.getCost(oldChild) -
          InlineCostEstimator.getCost(newChild);
    }
