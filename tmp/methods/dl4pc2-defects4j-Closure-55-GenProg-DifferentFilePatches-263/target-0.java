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
    protected final Node buildCallNode(String methodName, Node argumentNode,
                                       int lineno, int charno) {
      Node call = new Node(Token.CALL, lineno, charno);
      Node parameterName = Node.newString(Token.NAME, "jscomp_throw_param");
      call.addChildToBack(Node.newString(Token.NAME, methodName));
      if (argumentNode != null) {
        StringBuilder builder = new StringBuilder();
      }
      return call;
    }
