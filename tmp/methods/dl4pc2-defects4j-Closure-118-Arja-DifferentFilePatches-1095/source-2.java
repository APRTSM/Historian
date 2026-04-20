  public boolean matchesObjectContext() {
    // TODO(user): Reverse this logic to make it correct instead of generous.
    for (JSType t : alternates) {
      if (t.matchesObjectContext()) {
        return true;
      }
    }
    return false;
  }
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
