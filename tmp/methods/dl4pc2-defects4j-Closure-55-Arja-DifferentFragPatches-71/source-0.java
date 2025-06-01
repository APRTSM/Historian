    protected final Node buildCallNode(String methodName, Node argumentNode,
                                       int lineno, int charno) {
      Node call = new Node(Token.CALL, lineno, charno);
      call.putBooleanProp(Node.FREE_CALL, true);
      call.addChildToBack(Node.newString(Token.NAME, methodName));
      if (argumentNode != null) {
        call.addChildToBack(argumentNode.cloneTree());
      }
      return call;
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
    int estimateSavings() {
      return InlineCostEstimator.getCost(oldChild) -
          InlineCostEstimator.getCost(newChild);
    }
