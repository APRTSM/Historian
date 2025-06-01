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
          n.removeChild(left);
          n.removeChild(right);
          parent.replaceChild(n, IR.getprop(left, right));
        }
        break;
    }
  }
    public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()) {
        case Token.WHILE:
          if (CONVERT_WHILE_TO_FOR) {
            Node expr = n.getFirstChild();
            n.setType(Token.FOR);
            Node empty = IR.empty();
            empty.copyInformationFrom(n);
            n.addChildBefore(empty, expr);
            n.addChildAfter(empty.cloneNode(), expr);
            reportCodeChange("WHILE node");
          }
          break;

        case Token.FUNCTION:
          normalizeFunctionDeclaration(n);
          break;

        case Token.NAME:
        case Token.STRING:
        case Token.STRING_KEY:
        case Token.GETTER_DEF:
        case Token.SETTER_DEF:
          if (!compiler.getLifeCycleStage().isNormalizedObfuscated()) {
            annotateConstantsByConvention(n, parent);
          }
          break;

        case Token.CAST:
          ;
          break;
      }
    }
