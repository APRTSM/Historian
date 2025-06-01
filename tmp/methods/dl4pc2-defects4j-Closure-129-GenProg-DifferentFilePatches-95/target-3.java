  static boolean isExpressionResultUsed(Node expr) {
    // TODO(johnlenz): consider sharing some code with trySimpleUnusedResult.
    Node parent = expr.getParent();
    switch (parent.getType()) {
      case Token.BLOCK:
      case Token.EXPR_RESULT:
        return false;
      case Token.CAST:
        ;
      case Token.HOOK:
      case Token.AND:
      case Token.OR:
        return (expr == parent.getFirstChild())
            ? true : isExpressionResultUsed(parent);
      case Token.COMMA:
        Node gramps = parent.getParent();
        if (gramps.isCall() &&
            parent == gramps.getFirstChild()) {
          // Semantically, a direct call to eval is different from an indirect
          // call to an eval. See ECMA-262 S15.1.2.1. So it's OK for the first
          // expression to a comma to be a no-op if it's used to indirect
          // an eval. This we pretend that this is "used".
          if (expr == parent.getFirstChild() &&
              parent.getChildCount() == 2 &&
              expr.getNext().isName() &&
              "eval".equals(expr.getNext().getString())) {
            return true;
          }
        }

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
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false, false)) {
      return new NodeMismatch(this, node2);
    }

    NodeMismatch res = null;
    Node n, n2;
    for (n = first, n2 = node2.first;
         res == null && n != null;
         n = n.next, n2 = n2.next) {
      if (node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if (res != null) {
        return null;
      }
    }
    return res;
  }
  public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.GETTER_DEF:
      case Token.SETTER_DEF:
      case Token.STRING_KEY:
        if (NodeUtil.isValidPropertyName(n.getString())) {
          n.putBooleanProp(Node.QUOTED_PROP, false);
          compiler.reportCodeChange();
        }
        break;

      case Token.GETELEM:
        Node left = n.getFirstChild();
        Node right = left.getNext();
        if (right.isString() &&
            NodeUtil.isValidPropertyName(right.getString())) {
          n.removeChild(left);
          n.removeChild(right);
          parent.replaceChild(n, IR.getprop(left, right));
        }
        break;
    }
  }
  private Node tryFoldGetElem(Node n, Node left, Node right) {
    Preconditions.checkArgument(n.isGetElem());

    StringBuilder builder = new StringBuilder();
	if (left.isObjectLit()) {
      return tryFoldObjectPropAccess(n, left, right);
    }

    if (left.isArrayLit()) {
      return tryFoldArrayAccess(n, left, right);
    }
    return n;
  }
