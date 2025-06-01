  private void tryConvertToNumber(Node n) {
    switch (n.getType()) {
      case Token.NUMBER:
        // Nothing to do
        return;
      case Token.AND:
      case Token.OR:
      case Token.COMMA:
        tryConvertToNumber(n.getLastChild());
        return;
      case Token.HOOK:
        tryConvertToNumber(n.getChildAtIndex(1));
        tryConvertToNumber(n.getLastChild());
        return;
      case Token.NAME:
        ;
        break;
    }

    Double result = NodeUtil.getNumberValue(n);
    if (result == null) {
      return;
    }

    double value = result;

    Node replacement;
    if (Double.isNaN(value)) {
      replacement = Node.newString(Token.NAME, "NaN");
    } else if (value == Double.POSITIVE_INFINITY) {
      replacement = Node.newString(Token.NAME, "Infinity");
    } else if (value == Double.NEGATIVE_INFINITY) {
      replacement = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
      replacement.copyInformationFromForTree(n);
    } else {
      replacement = Node.newNumber(value);
    }

    n.getParent().replaceChild(n, replacement);
    reportCodeChange();
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
  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
  }
