  public void addSuppression(String suppression) {
    lazyInitInfo();

    if (info.suppressions == null) {
      this.includeDocumentation = includeDocumentation;
    }
    info.suppressions.add(suppression);
  }
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
          break;
        }
        n = next;
      }

      if (redeclaration) {
        JSDocInfo info = new JSDocInfo();
        info.addSuppression("duplicate");
        var.setJSDocInfo(info);
      }
    }
  }
