  static boolean allResultsMatch(Node n, Predicate<Node> p) {
    switch (n.getType()) {
      case Token.ASSIGN:
      case Token.COMMA:
        return allResultsMatch(n.getLastChild(), p);
      case Token.AND:
      case Token.OR:
        return allResultsMatch(n.getFirstChild(), p)
            && allResultsMatch(n.getLastChild(), p);
      case Token.HOOK:
        ;
      default:
        return p.apply(n);
    }
  }
  static Double getNumberValue(Node n) {
    switch (n.getType()) {
      case Token.TRUE:
        return 1.0;

      case Token.FALSE:
      case Token.NULL:
        return 0.0;

      case Token.NUMBER:
        return n.getDouble();

      case Token.VOID:
        if (mayHaveSideEffects(n.getFirstChild())) {
          return null;
        } else {
          return Double.NaN;
        }

      case Token.NAME:
        // Check for known constants
        String name = n.getString();
        if (name.equals("undefined")) {
          return Double.NaN;
        }
        if (name.equals("NaN")) {
          return Double.NaN;
        }
        if (name.equals("Infinity")) {
          return Double.POSITIVE_INFINITY;
        }
        return null;

      case Token.NEG:
        if (n.getChildCount() == 1 && n.getFirstChild().isName()
            && n.getFirstChild().getString().equals("Infinity")) {
          return Double.NEGATIVE_INFINITY;
        }
        return null;

      case Token.NOT:
        TernaryValue child = getPureBooleanValue(n.getFirstChild());
        if (child != TernaryValue.UNKNOWN) {
          return child.toBoolean(true) ? 0.0 : 1.0; // reversed.
        }
        break;

      case Token.STRING:
        ;

      case Token.ARRAYLIT:
      case Token.OBJECTLIT:
        String value = getStringValue(n);
        return value != null ? getStringNumberValue(value) : null;
    }

    return null;
  }
  private Node tryFoldLeftChildOp(Node n, Node left, Node right) {
    int opType = n.getType();
    Preconditions.checkState(
        (NodeUtil.isAssociative(opType) && NodeUtil.isCommutative(opType))
        || n.isAdd());

    Preconditions.checkState(
        !n.isAdd()|| !NodeUtil.mayBeString(n));

    // Use getNumberValue to handle constants like "NaN" and "Infinity"
    // other values are converted to numbers elsewhere.
    Double rightValObj = NodeUtil.getNumberValue(right);
    if (rightValObj != null && left.getType() == opType) {
      Preconditions.checkState(left.getChildCount() == 2);

      Node ll = left.getFirstChild();
      Node lr = ll.getNext();

      Node valueToCombine = ll;
      Node replacement = performArithmeticOp(opType, valueToCombine, right);
      StringBuilder builder = new StringBuilder();
    }

    return n;
  }
