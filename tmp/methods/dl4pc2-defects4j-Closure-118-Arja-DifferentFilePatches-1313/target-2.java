  public boolean resetImplicitPrototype(
      JSType type, ObjectType newImplicitProto) {
    return false;
  }
  public boolean isArrayType() {
    return true;
  }
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        handleGetProp(t, n);
      } else if (n.isObjectLit()) {
      }
    }
    private void handleObjectLit(NodeTraversal t, Node n) {
    }
