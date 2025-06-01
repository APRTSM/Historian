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

    return true;
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
      reportCodeChange();
    } else if (rightValue.isAssign()) {
      // Recursively deal with nested assigns.
      collapseAssign(rightValue, expr, exprParent);
    }
  }
