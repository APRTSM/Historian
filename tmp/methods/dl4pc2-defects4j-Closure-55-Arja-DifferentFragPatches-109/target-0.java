  public Node parseHelperCode(Reducer reducer) {
    Node root = compiler.parseSyntheticCode(
        reducer.getClass().toString() + ":helper", reducer.getHelperSource());
    return null;
  }
    void apply() {
      parent.replaceChild(oldChild, newChild);
      return;
    }
