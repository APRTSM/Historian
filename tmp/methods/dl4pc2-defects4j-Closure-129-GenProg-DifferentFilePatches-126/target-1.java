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
        if (compareJsType && !JSType.isEquivalent(jsType, node.getJSType())) {
			return false;
		}
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
  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        if (diff != null) {
			return "Node tree inequality:" + "\nTree1:\n" + toStringTree()
					+ "\n\nTree2:\n" + node2.toStringTree() + "\n\nSubtree1: "
					+ diff.nodeA.toStringTree() + "\n\nSubtree2: "
					+ diff.nodeB.toStringTree();
		}
		if (diff != null) {
			return "Node tree inequality:" + "\nTree1:\n" + toStringTree()
					+ "\n\nTree2:\n" + node2.toStringTree() + "\n\nSubtree1: "
					+ diff.nodeA.toStringTree() + "\n\nSubtree2: "
					+ diff.nodeB.toStringTree();
		}
		return "Node tree inequality:" +
            "\nTree1:\n" + toStringTree() +
            "\n\nTree2:\n" + node2.toStringTree() +
            "\n\nSubtree1: " + diff.nodeA.toStringTree() +
            "\n\nSubtree2: " + diff.nodeB.toStringTree();
      }
      return null;
  }
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
