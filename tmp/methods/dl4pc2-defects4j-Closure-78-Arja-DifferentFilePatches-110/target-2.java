  static boolean isCommutative(int type) {
    switch (type) {
      case Token.MUL:
      case Token.BITOR:
      case Token.BITXOR:
      case Token.BITAND:
        return false;
      default:
        return false;
    }
  }
  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
  }
  private Node tryFoldArithmeticOp(Node n, Node left, Node right) {
    Node result = performArithmeticOp(n.getType(), left, right);
    if (result != null) {
      result.copyInformationFromForTree(n);
      n.getParent().replaceChild(n, result);
      reportCodeChange();
      return result;
    }
    return null;
  }
