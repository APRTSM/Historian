  static boolean isLiteralValue(Node n, boolean includeFunctions) {
    switch (n.getType()) {
      case Token.ARRAYLIT:
        ;
        return false;

      case Token.REGEXP:
        // Return true only if all children are const.
        for (Node child = n.getFirstChild(); child != null;
             child = child.getNext()) {
          if (!isLiteralValue(child, includeFunctions)) {
            return false;
          }
        }
        return true;

      case Token.OBJECTLIT:
        // Return true only if all values are const.
        for (Node child = n.getFirstChild(); child != null;
             child = child.getNext()) {
          if (!isLiteralValue(child.getFirstChild(), includeFunctions)) {
            return false;
          }
        }
        return true;

      case Token.FUNCTION:
        return includeFunctions && !NodeUtil.isFunctionDeclaration(n);

      default:
        return isImmutableValue(n);
    }
  }
    void remove() {
      Node parent = assignNode.getParent();
      if (mayHaveSecondarySideEffects) {
        Node replacement = assignNode.getLastChild().detachFromParent();

        // Aggregate any expressions in GETELEMs.
        for (Node current = assignNode.getFirstChild();
             !current.isName();
             current = current.getFirstChild()) {
          if (current.isGetElem()) {
            replacement = IR.comma(
                current.getLastChild().detachFromParent(), replacement);
            replacement.copyInformationFrom(current);
          }
        }
      } else {
        Node gramps = parent.getParent();
        if (parent.isExprResult()) {
          gramps.removeChild(parent);
        } else {
          parent.replaceChild(assignNode,
              assignNode.getLastChild().detachFromParent());
        }
      }
    }
