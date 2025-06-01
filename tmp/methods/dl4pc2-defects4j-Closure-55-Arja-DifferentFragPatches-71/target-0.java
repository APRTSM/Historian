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
    protected final Node buildCallNode(String methodName, Node argumentNode,
                                       int lineno, int charno) {
      Node call = new Node(Token.CALL, lineno, charno);
      call.putBooleanProp(Node.FREE_CALL, true);
      if (argumentNode != null) {
        call.addChildToBack(argumentNode.cloneTree());
      }
      return call;
    }
    int estimateSavings() {
      return 0;
    }
