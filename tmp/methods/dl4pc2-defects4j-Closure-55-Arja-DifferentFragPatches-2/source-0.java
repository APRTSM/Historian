  public Node parseHelperCode(Reducer reducer) {
    Node root = compiler.parseSyntheticCode(
        reducer.getClass().toString() + ":helper", reducer.getHelperSource());
    return (root != null) ? root.removeFirstChild() : null;
  }
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
