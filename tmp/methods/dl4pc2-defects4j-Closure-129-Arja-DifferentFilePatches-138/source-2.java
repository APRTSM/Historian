  private Node tryFoldGetElem(Node n, Node left, Node right) {
    Preconditions.checkArgument(n.isGetElem());

    if (left.isObjectLit()) {
      return tryFoldObjectPropAccess(n, left, right);
    }

    if (left.isArrayLit()) {
      return tryFoldArrayAccess(n, left, right);
    }
    return n;
  }
  boolean isEquivalentTo(
      Node node, boolean compareJsType, boolean recur, boolean shallow) {
    if (type != node.getType()
        || getChildCount() != node.getChildCount()
        || this.getClass() != node.getClass()) {
      return false;
    }

    if (compareJsType && !JSType.isEquivalent(jsType, node.getJSType())) {
      return false;
    }

    if (type == Token.INC || type == Token.DEC) {
      int post1 = this.getIntProp(INCRDECR_PROP);
      int post2 = node.getIntProp(INCRDECR_PROP);
      if (post1 != post2) {
        return false;
      }
    } else if (type == Token.STRING || type == Token.STRING_KEY) {
      if (type == Token.STRING_KEY) {
        int quoted1 = this.getIntProp(QUOTED_PROP);
        int quoted2 = node.getIntProp(QUOTED_PROP);
        if (quoted1 != quoted2) {
          return false;
        }
      }

      int slashV1 = this.getIntProp(SLASH_V);
      int slashV2 = node.getIntProp(SLASH_V);
      if (slashV1 != slashV2) {
        return false;
      }
    } else if (type == Token.CALL) {
      if (this.getBooleanProp(FREE_CALL) != node.getBooleanProp(FREE_CALL)) {
        return false;
      }
    }

    if (recur) {
      Node n, n2;
      for (n = first, n2 = node.first;
           n != null;
           n = n.next, n2 = n2.next) {
        if (!n.isEquivalentTo(
            n2, compareJsType, !(shallow && n.isFunction()), shallow)) {
          return false;
        }
      }
    }

    return true;
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
          compiler.reportCodeChange();
        }
        break;
    }
  }
