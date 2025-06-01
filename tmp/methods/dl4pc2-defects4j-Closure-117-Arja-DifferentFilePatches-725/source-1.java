  private FlowScope narrowScope(FlowScope scope, Node node, JSType narrowed) {
    if (node.isThis()) {
      // "this" references don't need to be modeled in the control flow graph.
      return scope;
    }

    scope = scope.createChildFlowScope();
    if (node.isGetProp()) {
      scope.inferQualifiedSlot(
          node, node.getQualifiedName(), getJSType(node), narrowed);
    } else {
      redeclareSimpleVar(scope, node, narrowed);
    }
    return scope;
  }
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isExprResult()) {
          Node child = n.getFirstChild();
          switch (child.getType()) {
            case Token.ASSIGN:
              maybeCollectMember(child.getFirstChild(), child,
                  child.getLastChild());
              break;
            case Token.GETPROP:
              maybeCollectMember(child, child, null);
              break;
          }
        }
      }
