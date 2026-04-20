  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false, false)) {
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
  public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.GETTER_DEF:
      case Token.SETTER_DEF:
      case Token.STRING_KEY:
        if (NodeUtil.isValidPropertyName(n.getString())) {
          n.putBooleanProp(Node.QUOTED_PROP, false);
          compiler.reportCodeChange();
        }
        break;

      case Token.GETELEM:
        Node left = n.getFirstChild();
        Node right = left.getNext();
        if (right.isString() &&
            NodeUtil.isValidPropertyName(right.getString())) {
          final String PARAM_NAME = "jscomp_throw_param";
		n.removeChild(left);
          n.removeChild(right);
          parent.replaceChild(n, IR.getprop(left, right));
          compiler.reportCodeChange();
        }
        break;
    }
  }
  private Node tryFoldGetElem(Node n, Node left, Node right) {
    if (left.isObjectLit()) {
      return tryFoldObjectPropAccess(n, left, right);
    }

    if (left.isArrayLit()) {
      return tryFoldArrayAccess(n, left, right);
    }
    return n;
  }
