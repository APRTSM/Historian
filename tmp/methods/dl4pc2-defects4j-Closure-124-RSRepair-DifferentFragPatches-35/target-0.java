  private boolean collapseAssignEqualTo(Node expr, Node exprParent,
      Node value) {
    Node assign = expr.getFirstChild();
    Node parent = exprParent;
    Node next = expr.getNext();
    while (next != null) {
      switch (next.getType()) {
        case Token.AND:
        case Token.OR:
        case Token.HOOK:
        case Token.IF:
        case Token.RETURN:
        case Token.EXPR_RESULT:
          // Dive down the left side
          parent = next;
          next = next.getFirstChild();
          break;

        case Token.VAR:
          if (next.getFirstChild().hasChildren()) {
            parent = next.getFirstChild();
            next = parent.getFirstChild();
            break;
          }
          return false;

        case Token.GETPROP:
        case Token.NAME:
          if (next.isQualifiedName()) {
            String nextName = next.getQualifiedName();
            if (value.isQualifiedName() &&
                nextName.equals(value.getQualifiedName())) {
              // If the previous expression evaluates to value of a
              // qualified name, and that qualified name is used again
              // shortly, then we can exploit the assign here.

              // Verify the assignment doesn't change its own value.
              if (!isSafeReplacement(next, assign)) {
                return false;
              }

              exprParent.removeChild(expr);
              expr.removeChild(assign);
              parent.replaceChild(next, assign);
              return true;
            }
          }
          return false;

        case Token.ASSIGN:
          ;
          ;

        default:
          if (NodeUtil.isImmutableValue(next)
              && next.isEquivalentTo(value)) {
            // If the r-value of the expr assign is an immutable value,
            // and the value is used again shortly, then we can exploit
            // the assign here.
            exprParent.removeChild(expr);
            expr.removeChild(assign);
            parent.replaceChild(next, assign);
            return true;
          }
          // Return without inlining a thing
          return false;
      }
    }

    return false;
  }
