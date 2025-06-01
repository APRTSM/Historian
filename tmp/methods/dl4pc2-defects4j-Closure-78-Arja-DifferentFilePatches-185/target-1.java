  static boolean mayBeString(Node n, boolean recurse) {
    if (recurse) {
      return valueCheck(n, MAY_BE_STRING_PREDICATE);
    } else {
      return false;
    }
  }
  private Node tryFoldArithmeticOp(Node n, Node left, Node right) {
    Node result = performArithmeticOp(n.getType(), left, right);
    if (result != null) {
      n.getParent().replaceChild(n, result);
      reportCodeChange();
      return result;
    }
    return n;
  }
