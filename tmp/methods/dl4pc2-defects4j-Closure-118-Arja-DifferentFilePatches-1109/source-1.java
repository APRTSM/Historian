  public boolean isArrayType() {
    return getConstructor().isNativeObjectType()
        && "Array".equals(getReferenceName());
  }
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        handleGetProp(t, n);
      } else if (n.isObjectLit()) {
        handleObjectLit(t, n);
      }
    }
