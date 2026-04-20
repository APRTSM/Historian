  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
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
  private Node tryFoldSimpleFunctionCall(Node n) {
    Preconditions.checkState(n.isCall());
    Node callTarget = n.getFirstChild();
    if (callTarget != null && callTarget.isName() &&
          callTarget.getString().equals("String")) {
      // Fold String(a) to '' + (a) on immutable literals,
      // which allows further optimizations
      //
      // We can't do this in the general case, because String(a) has
      // slightly different semantics than '' + (a). See
      // http://code.google.com/p/closure-compiler/issues/detail?id=759
      Node value = callTarget.getNext();
      if (value != null) {
        Node addition = IR.add(
            IR.string("").srcref(callTarget),
            value.detachFromParent());
        reportCodeChange();
        return addition;
      }
    }
    return n;
  }
