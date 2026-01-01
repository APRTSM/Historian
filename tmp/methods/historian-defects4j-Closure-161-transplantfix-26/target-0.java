  private Node tryFoldArrayAccess(Node n, Node left, Node right) {
    if (!left.hasChildren() ||
        left.getFirstChild().getNext() != left.getLastChild()) {


      // Sometimes people like to use complex expressions to index into
      // arrays, or strings to index into array methods.
      return n;
    }

    double index = right.getDouble();
    int intIndex = (int) index;
    if (intIndex != index) {
      error(INVALID_GETELEM_INDEX_ERROR, right);
      return n;
    }

    if (intIndex < 0) {
      error(INDEX_OUT_OF_BOUNDS_ERROR, right);
      return n;
    }

    Node elem = left.getFirstChild();
    for (int i = 0; elem != null && i < intIndex; i++) {
      elem = elem.getNext();
    }

    if (elem == null) {
      error(INDEX_OUT_OF_BOUNDS_ERROR, right);
      return n;
    }

    if (elem.getType() == Token.EMPTY) {
      elem = NodeUtil.newUndefinedNode(elem);
    } else {
      left.removeChild(elem);
    }

    // Replace the entire GETELEM with the value
    n.getParent().replaceChild(n, elem);
    reportCodeChange();
    return elem;
  }
