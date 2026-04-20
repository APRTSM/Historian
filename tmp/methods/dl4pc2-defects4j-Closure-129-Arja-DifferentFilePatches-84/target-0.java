  private Node tryFoldGetElem(Node n, Node left, Node right) {
    if (left.isObjectLit()) {
      return tryFoldObjectPropAccess(n, left, right);
    }

    return n;
  }
