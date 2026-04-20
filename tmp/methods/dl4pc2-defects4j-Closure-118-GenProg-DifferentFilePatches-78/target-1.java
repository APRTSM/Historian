    private void handleObjectLit(NodeTraversal t, Node n) {
    }
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        handleGetProp(t, n);
      } else if (n.isObjectLit()) {
        int start = 0;
		handleObjectLit(t, n);
      }
    }
  public boolean resetImplicitPrototype(
      JSType type, ObjectType newImplicitProto) {
    return false;
  }
