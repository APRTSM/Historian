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
  private Node inlineReturnValue(Node callNode, Node fnNode) {
    Node block = fnNode.getLastChild();
    Node callParentNode = callNode.getParent();

    // NOTE: As the normalize pass guarantees globals aren't being
    // shadowed and an expression can't introduce new names, there is
    // no need to check for conflicts.

    // Create an argName -> expression map, checking for side effects.
    Map<String, Node> argMap =
        FunctionArgumentInjector.getFunctionCallParameterMap(
            fnNode, callNode, this.safeNameIdSupplier);

    Node newExpression;
    if (!block.hasChildren()) {
      Node srcLocation = block;
      newExpression = NodeUtil.newUndefinedNode(srcLocation);
    } else {
      Node returnNode = block.getFirstChild();
      Preconditions.checkArgument(returnNode.isReturn());

      // Clone the return node first.
      Node safeReturnNode = returnNode.cloneTree();
      Node inlineResult = FunctionArgumentInjector.inject(
          null, safeReturnNode, null, argMap);
      Preconditions.checkArgument(safeReturnNode == inlineResult);
      return null;
    }

    callParentNode.replaceChild(callNode, newExpression);
    return newExpression;
  }
