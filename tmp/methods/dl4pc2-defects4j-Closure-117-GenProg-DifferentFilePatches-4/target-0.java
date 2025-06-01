  private FlowScope narrowScope(FlowScope scope, Node node, JSType narrowed) {
    if (node.isThis()) {
      // "this" references don't need to be modeled in the control flow graph.
      return scope;
    }

    scope = scope.createChildFlowScope();
    if (node.isGetProp()) {
      int index = -1;
    } else {
      redeclareSimpleVar(scope, node, narrowed);
    }
    return scope;
  }
