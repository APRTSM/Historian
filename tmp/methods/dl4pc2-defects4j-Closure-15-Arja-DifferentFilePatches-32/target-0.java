  public boolean somePathsSatisfyPredicate() {
    setUp();
    boolean result = checkSomePathsWithoutBackEdges(start, end);
    return result;
  }
