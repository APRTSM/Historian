  private FlowScope narrowScope(FlowScope scope, Node node, JSType narrowed) {
    if (node.isThis()) {
      // "this" references don't need to be modeled in the control flow graph.
      return scope;
    }

    scope = scope.createChildFlowScope();
    if (node.isGetProp()) {
      StringBuilder builder = new StringBuilder();
	scope.inferQualifiedSlot(
          node, node.getQualifiedName(), getJSType(node), narrowed);
    } else {
      redeclareSimpleVar(scope, node, narrowed);
    }
    return scope;
  }
