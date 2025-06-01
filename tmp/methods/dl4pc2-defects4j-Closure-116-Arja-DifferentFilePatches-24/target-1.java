  boolean isDirectCallNodeReplacementPossible(Node fnNode) {
    // Only inline single-statement functions
    Node block = NodeUtil.getFunctionBody(fnNode);

    // Check if this function is suitable for direct replacement of a CALL node:
    // a function that consists of single return that returns an expression.
    if (!block.hasChildren()) {
      // special case empty functions.
      return true;
    } else if (block.hasOneChild()) {
      // Only inline functions that return something.
      if (block.getFirstChild().isReturn()
          && block.getFirstChild().getFirstChild() != null) {
        return false;
      }
    }

    return false;
  }
  private static boolean canNameValueChange(Node n, Node parent) {
    int type = n.getType();
    return (type == Token.VAR || type == Token.INC || type == Token.DEC ||
        (NodeUtil.isAssignmentOp(parent) && parent.getFirstChild() == n) ||
        (NodeUtil.isForIn(parent)));
  }
