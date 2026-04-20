    protected final Node maybeGetSingleReturnRValue(Node functionNode) {
      Node body = functionNode.getLastChild();
      if (!body.hasOneChild()) {
        return null;
      }

      Node statement = body.getFirstChild();
      if (statement.getType() == Token.RETURN) {
        return statement.getFirstChild();
      }
      return null;
    }
    public Node reduce(Node node) {
      if (!isReduceableFunctionExpression(node)) {
        return node;
      }

      Node propName = getGetPropertyName(node);
      if (propName != null) {
        if (propName.getType() != Token.STRING) {
          throw new IllegalStateException(
              "Expected STRING, got " + Token.name(propName.getType()));
        }

        return buildCallNode(FACTORY_METHOD_NAME, propName,
                             node.getLineno(), node.getCharno());
      } else {
        return node;
      }
    }
