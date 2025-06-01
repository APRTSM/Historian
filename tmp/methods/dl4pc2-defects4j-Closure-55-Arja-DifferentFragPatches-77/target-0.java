    int estimateSavings() {
      return 0;
    }
    private Node getSetPropertyName(Node functionNode) {
      Node body = functionNode.getLastChild();
      if (!body.hasOneChild()) {
      }

      Node argList = functionNode.getFirstChild().getNext();
      Node paramNode = argList.getFirstChild();
      if (paramNode == null) {
        return null;
      }

      Node statement = body.getFirstChild();
      if (!NodeUtil.isExprAssign(statement)) {
        return null;
      }

      Node assign = statement.getFirstChild();
      Node lhs = assign.getFirstChild();
      if (NodeUtil.isGetProp(lhs) && NodeUtil.isThis(lhs.getFirstChild())) {
        Node rhs = assign.getLastChild();
        if (NodeUtil.isName(rhs) &&
            rhs.getString().equals(paramNode.getString())) {
          Node propertyName = lhs.getLastChild();
          return propertyName;
        }
      }
      return null;
    }
