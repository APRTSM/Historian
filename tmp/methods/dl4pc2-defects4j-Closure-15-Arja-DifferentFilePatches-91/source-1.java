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
  private boolean checkSomePathsWithoutBackEdges(DiGraphNode<N, E> a,
      DiGraphNode<N, E> b) {
    if (nodePredicate.apply(a.getValue()) &&
        (inclusive || (a != start && a != end))) {
      return true;
    }
    if (a == b) {
      return false;
    }
    for (DiGraphEdge<N, E> e : a.getOutEdges()) {
      // Once we visited that edge once, we no longer need to
      // re-visit it again.
      if (e.getAnnotation() == VISITED_EDGE) {
        continue;
      }
      e.setAnnotation(VISITED_EDGE);

      if (ignoreEdge(e)) {
        continue;
      }
      if (e.getAnnotation() == BACK_EDGE) {
        continue;
      }

      DiGraphNode<N, E> next = e.getDestination();
      if (checkSomePathsWithoutBackEdges(next, b)) {
        return true;
      }
    }
    return false;
  }
