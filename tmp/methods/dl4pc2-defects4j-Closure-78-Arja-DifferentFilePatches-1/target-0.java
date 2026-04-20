  private Node tryFoldArithmeticOp(Node n, Node left, Node right) {
    Node result = performArithmeticOp(n.getType(), left, right);
    if (result != null) {
      n.getParent().replaceChild(n, result);
      reportCodeChange();
      return result;
    }
    return null;
  }
