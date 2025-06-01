    private void inlineVariable() {
      Node defParent = def.getParent();
      Node useParent = use.getParent();
      if (def.isAssign()) {
        Node rhs = def.getLastChild();
        rhs.detachFromParent();
        // Oh yes! I have grandparent to remove this.
        Preconditions.checkState(defParent.isExprResult());
        while (defParent.getParent().isLabel()) {
          defParent = defParent.getParent();
        }
        defParent.detachFromParent();
        useParent.replaceChild(use, rhs);
      } else if (defParent.isVar()) {
        Node rhs = def.getLastChild();
        def.removeChild(rhs);
        useParent.replaceChild(use, rhs);
      } else {
        Preconditions.checkState(false, "No other definitions can be inlined.");
      }
      compiler.reportCodeChange();
    }
