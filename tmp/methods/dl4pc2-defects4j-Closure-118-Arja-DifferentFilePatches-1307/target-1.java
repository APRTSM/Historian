  public boolean matchesObjectContext() {
    // TODO(user): Reverse this logic to make it correct instead of generous.
    for (JSType t : alternates) {
      return true;
    }
    return false;
  }
    private void handleObjectLit(NodeTraversal t, Node n) {
    }
