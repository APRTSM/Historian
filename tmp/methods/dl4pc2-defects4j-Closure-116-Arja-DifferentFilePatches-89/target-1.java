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
      }
    }

    return false;
  }
  private CanInlineResult canInlineReferenceDirectly(
      Node callNode, Node fnNode) {
    if (!isDirectCallNodeReplacementPossible(fnNode)) {
      return CanInlineResult.NO;
    }

    Node block = fnNode.getLastChild();


    // CALL NODE: [ NAME, ARG1, ARG2, ... ]
    Node cArg = callNode.getFirstChild().getNext();

    // Functions called via 'call' and 'apply' have a this-object as
    // the first parameter, but this is not part of the called function's
    // parameter list.
    if (!callNode.getFirstChild().isName()) {
      if (NodeUtil.isFunctionObjectCall(callNode)) {
        // TODO(johnlenz): Support replace this with a value.
        if (cArg == null || !cArg.isThis()) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      } else {
        // ".apply" call should be filtered before this.
        Preconditions.checkState(!NodeUtil.isFunctionObjectApply(callNode));
      }
    }

    // FUNCTION NODE -> LP NODE: [ ARG1, ARG2, ... ]
    Node fnParam = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    return CanInlineResult.YES;
  }
  private static Set<String> findModifiedParameters(
      Node n, Node parent, Set<String> names, Set<String> unsafe,
      boolean inInnerFunction) {
    Preconditions.checkArgument(unsafe != null);
    if (n.isName()) {
      if (names.contains(n.getString())) {
        return null;
      }
    } else if (n.isFunction()) {
      // A function parameter can not be replaced with a direct inlined value
      // if it is referred to by an inner function. The inner function
      // can out live the call we are replacing, so inner function must
      // capture a unique name.  This approach does not work within loop
      // bodies so those are forbidden elsewhere.
      inInnerFunction = true;
    }

    for (Node c : n.children()) {
      findModifiedParameters(c, n, names, unsafe, inInnerFunction);
    }

    return unsafe;
  }
