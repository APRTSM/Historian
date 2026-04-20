  private Node tryFoldGetElem(Node n, Node left, Node right) {
    Preconditions.checkArgument(n.isGetElem());

    if (left.isObjectLit()) {
      return tryFoldObjectPropAccess(n, left, right);
    }

    return null;
  }
