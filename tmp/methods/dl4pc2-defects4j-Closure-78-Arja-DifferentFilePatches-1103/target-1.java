  private Node tryFoldLeftChildOp(Node n, Node left, Node right) {
    int opType = n.getType();
    Preconditions.checkState(
        n.getType() != Token.ADD || !NodeUtil.mayBeString(n));

    // Use getNumberValue to handle constants like "NaN" and "Infinity"
    // other values are converted to numbers elsewhere.
    Double rightValObj = NodeUtil.getNumberValue(right);
    return n;
  }
  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
  }
