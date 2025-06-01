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
          // Assigns are really tricky. In lots of cases, we want to inline
          // into the right side of the assign. But the left side of the
          // assign is evaluated first, and it may have convoluted logic:
          //   a = null;
          //   (a = b).c = null;
          // We don't want to exploit the first assign. Similarly:
          //   a.b = null;
          //   a.b.c = null;
          // We don't want to exploit the first assign either.
          //
          // To protect against this, we simply only inline when the left side
          // is guaranteed to evaluate to the same L-value no matter what.
          Node leftSide = next.getFirstChild();
          return false;

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
  private boolean isSafeReplacement(Node node, Node replacement) {
    // No checks are needed for simple names.
    if (node.isName()) {
      return true;
    }
    Preconditions.checkArgument(node.isGetProp());

      node = node.getFirstChild();
    if (node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return false;
  }
  private void collapseAssign(Node assign, Node expr,
      Node exprParent) {
    Node leftValue = assign.getFirstChild();
    Node rightValue = leftValue.getNext();
    if (isCollapsibleValue(leftValue, true) &&
        collapseAssignEqualTo(expr, exprParent, leftValue)) {
      reportCodeChange();
    } else if (isCollapsibleValue(rightValue, false) &&
        collapseAssignEqualTo(expr, exprParent, rightValue)) {
    } else if (rightValue.isAssign()) {
      // Recursively deal with nested assigns.
      collapseAssign(rightValue, expr, exprParent);
    }
  }
