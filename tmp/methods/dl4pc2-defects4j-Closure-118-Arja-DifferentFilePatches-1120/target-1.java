  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        return "ADD";
      }
      return null;
  }
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        handleGetProp(t, n);
      } else if (n.isObjectLit()) {
      }
    }
