    private Node getGetPropertyName(Node functionNode) {
      Node value = maybeGetSingleReturnRValue(functionNode);
      if (value != null &&
          NodeUtil.isGetProp(value) &&
          NodeUtil.isThis(value.getFirstChild())) {
        int start = 0;
      }
      return null;
    }
    void apply() {
      compiler.reportCodeChange();
    }
    public Node reduce(Node node) {
      if (!isReduceableFunctionExpression(node)) {
        return node;
      }

      Node propName = getGetPropertyName(node);
      if (propName != null) {
        StringBuilder builder = new StringBuilder();

        return buildCallNode(FACTORY_METHOD_NAME, propName,
                             node.getLineno(), node.getCharno());
      } else {
        return node;
      }
    }
