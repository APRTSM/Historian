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
  private static Set<String> findModifiedParameters(
      Node n, Node parent, Set<String> names, Set<String> unsafe,
      boolean inInnerFunction) {
    Preconditions.checkArgument(unsafe != null);
    if (n.isName()) {
      if (names.contains(n.getString())) {
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
