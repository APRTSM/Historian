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
      }
    }
    return false;
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
        return null;
      }
    }
    return res;
  }
