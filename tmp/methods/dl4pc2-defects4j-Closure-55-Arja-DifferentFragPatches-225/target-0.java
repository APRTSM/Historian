    public Node reduce(Node node) {
      if (!isReduceableFunctionExpression(node)) {
        return node;
      }

      Node propName = getGetPropertyName(node);
      if (propName != null) {
        return buildCallNode(FACTORY_METHOD_NAME, propName,
                             node.getLineno(), node.getCharno());
      } else {
        return node;
      }
    }
    int estimateSavings() {
      return 0;
    }
