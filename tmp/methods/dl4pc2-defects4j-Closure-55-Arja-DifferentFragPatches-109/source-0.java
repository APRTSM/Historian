  public Node parseHelperCode(Reducer reducer) {
    Node root = compiler.parseSyntheticCode(
        reducer.getClass().toString() + ":helper", reducer.getHelperSource());
    return (root != null) ? root.removeFirstChild() : null;
  }
    void apply() {
      parent.replaceChild(oldChild, newChild);
      compiler.reportCodeChange();
    }
