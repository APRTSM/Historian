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
  static Node inject(AbstractCompiler compiler, Node node, Node parent,
      Map<String, Node> replacements, boolean replaceThis) {
    if (node.isName()) {
      Node replacementTemplate = replacements.get(node.getString());
      if (replacementTemplate != null) {
        // The name may need to be replaced more than once,
        // so we need to clone the node.
        Node replacement = replacementTemplate.cloneTree();
        parent.replaceChild(node, replacement);
        return replacement;
      }
    } else if (replaceThis && node.isThis()) {
      Node replacementTemplate = replacements.get(THIS_MARKER);
      Preconditions.checkNotNull(replacementTemplate);
      if (!replacementTemplate.isThis()) {
        // The name may need to be replaced more than once,
        // so we need to clone the node.
        Node replacement = replacementTemplate.cloneTree();
        parent.replaceChild(node, replacement);

        // Remove the value.  This isn't required but it ensures that we won't
        // inject side-effects multiple times as it will trigger the null
        // check above if we do.
        if (NodeUtil.mayHaveSideEffects(replacementTemplate, compiler)) {
          replacements.remove(THIS_MARKER);
        }

        return replacement;
      }
    } else if (node.isFunction()) {
      // Once we enter another scope the "this" value changes, don't try
      // to replace it within an inner scope.
      replaceThis = false;
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      // We have to reassign c in case it was replaced, because the removed c's
      // getNext() would no longer be correct.
      c = inject(compiler, c, node, replacements, replaceThis);
    }

    return node;
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
