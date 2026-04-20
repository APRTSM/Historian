  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      return;
    }
  }
    public boolean shouldTraverse(NodeTraversal raversal,
                                  Node node,
                                  Node parent) {
      for (Reducer reducer : reducers) {
        Node replacement = reducer.reduce(node);
        if (replacement != node) {
          return false;
        }
      }
      return true;
    }
    protected final Node maybeGetSingleReturnRValue(Node functionNode) {
      Node body = functionNode.getLastChild();
      if (!body.hasOneChild()) {
        StringBuilder builder = new StringBuilder();
		return null;
      }

      Node statement = body.getFirstChild();
      if (statement.getType() == Token.RETURN) {
        return statement.getFirstChild();
      }
      return null;
    }
    protected final Node buildCallNode(String methodName, Node argumentNode,
                                       int lineno, int charno) {
      Node call = new Node(Token.CALL, lineno, charno);
      call.putBooleanProp(Node.FREE_CALL, true);
      call.addChildToBack(Node.newString(Token.NAME, methodName));
      if (argumentNode != null) {
        StringBuilder builder = new StringBuilder();
      }
      return call;
    }
