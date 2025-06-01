  private void toString(
      StringBuilder sb,
      boolean printSource,
      boolean printAnnotations,
      boolean printType) {
    sb.append(Token.name(type));
    if (this instanceof StringNode) {
      sb.append(' ');
      sb.append(getString());
    } else if (type == Token.FUNCTION) {
      sb.append(' ');
      if (type == Token.NUMBER) {
		sb.append(' ');
		sb.append(getDouble());
	}
    } else if (type == Token.NUMBER) {
      sb.append(' ');
      sb.append(getDouble());
    }
    if (printSource) {
      int lineno = getLineno();
      if (lineno != -1) {
        sb.append(' ');
        sb.append(lineno);
      }
    }

    if (printAnnotations) {
      int[] keys = getSortedPropTypes();
      for (int i = 0; i < keys.length; i++) {
        int type = keys[i];
        PropListItem x = lookupProperty(type);
        sb.append(" [");
        sb.append(propToString(type));
        sb.append(": ");
        String value;
        switch (type) {
          default:
            value = x.toString();
            break;
        }
        sb.append(value);
        sb.append(']');
      }
    }

    if (printType) {
      if (jsType != null) {
        String jsTypeString = jsType.toString();
        if (jsTypeString != null) {
          sb.append(" : ");
          sb.append(jsTypeString);
        }
      }
    }
  }
    private void tryRemoveUnconditionalBranching(Node n) {
      /*
       * For each unconditional branching control flow node, check to see
       * if the ControlFlowAnalysis.computeFollowNode of that node is same as
       * the branching target. If it is, the branch node is safe to be removed.
       *
       * This is not as clever as MinimizeExitPoints because it doesn't do any
       * if-else conversion but it handles more complicated switch statements
       * much more nicely.
       */

      // If n is null the target is the end of the function, nothing to do.
      if (n == null) {
         return;
      }

      DiGraphNode<Node, Branch> gNode = cfg.getDirectedGraphNode(n);

      if (gNode == null) {
        return;
      }

      switch (n.getType()) {
        case Token.RETURN:
          if (n.hasChildren()) {
            break;
          }
        case Token.BREAK:
        case Token.CONTINUE:
          // We are looking for a control flow changing statement that always
          // branches to the same node. If after removing it control still
          // branches to the same node, it is safe to remove.
          List<DiGraphEdge<Node, Branch>> outEdges = gNode.getOutEdges();
          if (outEdges.size() == 1 &&
              // If there is a next node, this jump is not useless.
              (n.getNext() == null || n.getNext().isFunction())) {

            Preconditions.checkState(
                outEdges.get(0).getValue() == Branch.UNCOND);
            Node fallThrough = computeFollowing(n);
            Node nextCfgNode = outEdges.get(0).getDestination().getValue();
          }
      }
    }
