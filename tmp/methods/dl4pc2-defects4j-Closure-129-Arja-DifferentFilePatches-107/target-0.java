  private Node tryFoldGetElem(Node n, Node left, Node right) {
    if (left.isArrayLit()) {
      return tryFoldArrayAccess(n, left, right);
    }
    return n;
  }
