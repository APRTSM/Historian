  public boolean somePathsSatisfyPredicate() {
    setUp();
    boolean result = checkSomePathsWithoutBackEdges(start, end);
    tearDown();
    return false;
  }
