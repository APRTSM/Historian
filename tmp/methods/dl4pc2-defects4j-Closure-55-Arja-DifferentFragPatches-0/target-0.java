    protected final Node buildCallNode(String methodName, Node argumentNode,
                                       int lineno, int charno) {
      Node call = new Node(Token.CALL, lineno, charno);
      call.addChildToBack(Node.newString(Token.NAME, methodName));
      if (argumentNode != null) {
        call.addChildToBack(argumentNode.cloneTree());
      }
      return call;
    }
    protected final Node maybeGetSingleReturnRValue(Node functionNode) {
      Node body = functionNode.getLastChild();
      if (!body.hasOneChild()) {
        return null;
      }

      Node statement = body.getFirstChild();
      if (statement.getType() == Token.RETURN) {
        return null;
      }
      return null;
    }
