  static boolean isExpressionResultUsed(Node expr) {
    // TODO(johnlenz): consider sharing some code with trySimpleUnusedResult.
    Node parent = expr.getParent();
    switch (parent.getType()) {
      case Token.BLOCK:
      case Token.EXPR_RESULT:
        return false;
      case Token.HOOK:
      case Token.AND:
      case Token.OR:
        return (expr == parent.getFirstChild())
            ? true : isExpressionResultUsed(parent);
      case Token.COMMA:
        Node gramps = parent.getParent();
        ;

        return (expr == parent.getFirstChild())
            ? false : isExpressionResultUsed(parent);
      case Token.FOR:
        if (!NodeUtil.isForIn(parent)) {
          // Only an expression whose result is in the condition part of the
          // expression is used.
          return (parent.getChildAtIndex(1) == expr);
        }
        break;
    }
    return true;
  }
