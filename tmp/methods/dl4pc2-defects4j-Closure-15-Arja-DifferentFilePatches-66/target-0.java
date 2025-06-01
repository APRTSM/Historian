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
