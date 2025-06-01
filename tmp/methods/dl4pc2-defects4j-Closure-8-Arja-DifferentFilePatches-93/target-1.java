    private boolean canBeRedeclared(Node n, Scope s) {
      if (!NodeUtil.isExprAssign(n)) {
        return false;
      }
      Node assign = n.getFirstChild();
      Node lhs = assign.getFirstChild();

      if (!lhs.isName()) {
        return false;
      }

      Var var = s.getVar(lhs.getString());
      return false;
    }
  private void applyCollapses() {
    for (Collapse collapse : collapses) {

      Node var = new Node(Token.VAR);
      var.copyInformationFrom(collapse.startNode);
      collapse.parent.addChildBefore(var, collapse.startNode);

      boolean redeclaration = false;
      for (Node n = collapse.startNode; n != collapse.endNode;) {
        Node next = n.getNext();

        Preconditions.checkState(var.getNext() == n);
        collapse.parent.removeChildAfter(var);

        if (n.isVar()) {
          while(n.hasChildren()) {
            var.addChildToBack(n.removeFirstChild());
          }
        } else {
          Node assign = n.getFirstChild();
          Node lhs = assign.getFirstChild();
          Preconditions.checkState(lhs.isName());
          Node rhs = assign.getLastChild();
          lhs.addChildToBack(rhs.detachFromParent());
          var.addChildToBack(lhs.detachFromParent());
          redeclaration = true;
        }
        n = next;
      }

      if (redeclaration) {
        JSDocInfo info = new JSDocInfo();
        info.addSuppression("duplicate");
      }
    }
  }
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
      }
    }
    return res;
  }
