  private boolean checkSomePathsWithoutBackEdges(DiGraphNode<N, E> a,
      DiGraphNode<N, E> b) {
    if (a == b) {
      return false;
    }
    for (DiGraphEdge<N, E> e : a.getOutEdges()) {
      // Once we visited that edge once, we no longer need to
      // re-visit it again.
      if (e.getAnnotation() == VISITED_EDGE) {
        continue;
      }
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
