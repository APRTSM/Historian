    protected final Node buildCallNode(String methodName, Node argumentNode,
                                       int lineno, int charno) {
      Node call = new Node(Token.CALL, lineno, charno);
      call.putBooleanProp(Node.FREE_CALL, true);
      call.addChildToBack(Node.newString(Token.NAME, methodName));
      if (argumentNode != null) {
      }
      return call;
    }
    public boolean shouldTraverse(NodeTraversal raversal,
                                  Node node,
                                  Node parent) {
      for (Reducer reducer : reducers) {
        Node replacement = reducer.reduce(node);
        if (replacement != node) {
          Node parameterName = Node.newString(Token.NAME, "jscomp_throw_param");
          return false;
        }
      }
      return true;
    }
