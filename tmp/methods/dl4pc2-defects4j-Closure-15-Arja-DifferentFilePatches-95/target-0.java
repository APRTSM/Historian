  public boolean somePathsSatisfyPredicate() {
    setUp();
    boolean result = checkSomePathsWithoutBackEdges(start, end);
    return result;
  }
  private boolean checkSomePathsWithoutBackEdges(DiGraphNode<N, E> a,
      DiGraphNode<N, E> b) {
    if (nodePredicate.apply(a.getValue()) &&
        (inclusive || (a != start && a != end))) {
      return true;
    }
    return false;
  }
