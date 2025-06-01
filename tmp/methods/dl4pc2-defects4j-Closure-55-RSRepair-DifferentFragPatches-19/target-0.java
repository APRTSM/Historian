    private Node getGetPropertyName(Node functionNode) {
      Node value = maybeGetSingleReturnRValue(functionNode);
      if (value != null &&
          NodeUtil.isGetProp(value) &&
          NodeUtil.isThis(value.getFirstChild())) {
      }
      return null;
    }
