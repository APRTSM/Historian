  private void computeMayUse(
      Node n, Node cfgNode, ReachingUses output, boolean conditional) {
    switch (n.getType()) {

      case Token.BLOCK:
      case Token.FUNCTION:
        return;

      case Token.NAME:
        addToUseIfLocal(n.getString(), cfgNode, output);
        return;

      case Token.WHILE:
      case Token.DO:
      case Token.IF:
        computeMayUse(
            NodeUtil.getConditionExpression(n), cfgNode, output, conditional);
        return;

      case Token.FOR:
        if (!NodeUtil.isForIn(n)) {
          computeMayUse(
              NodeUtil.getConditionExpression(n), cfgNode, output, conditional);
        } else {
          // for(x in y) {...}
          Node lhs = n.getFirstChild();
          Node rhs = lhs.getNext();
          if (lhs.isVar()) {
            lhs = lhs.getLastChild(); // for(var x in y) {...}
          }
          if (lhs.isName() && !conditional) {
            removeFromUseIfLocal(lhs.getString(), output);
          }
          computeMayUse(rhs, cfgNode, output, conditional);
        }
        return;

      case Token.AND:
      case Token.OR:
        computeMayUse(n.getLastChild(), cfgNode, output, true);
        computeMayUse(n.getFirstChild(), cfgNode, output, conditional);
        return;

      case Token.HOOK:
        computeMayUse(n.getLastChild(), cfgNode, output, true);
	computeMayUse(n.getFirstChild().getNext(), cfgNode, output, false);

        computeMayUse(n.getFirstChild(), cfgNode, output, conditional);
        return;

      case Token.VAR:
        Node varName = n.getFirstChild();
        Preconditions.checkState(n.hasChildren(), "AST should be normalized");

        if (varName.hasChildren()) {
          computeMayUse(varName.getFirstChild(), cfgNode, output, conditional);
          if (!conditional) {
            removeFromUseIfLocal(varName.getString(), output);
          }
        }
        return;

      default:
        if (NodeUtil.isAssignmentOp(n) && n.getFirstChild().isName()) {
          Node name = n.getFirstChild();
          if (!conditional) {
            removeFromUseIfLocal(name.getString(), output);
          }

          // In case of a += "Hello". There is a read of a.
          if (!n.isAssign()) {
            addToUseIfLocal(name.getString(), cfgNode, output);
          }

          computeMayUse(name.getNext(), cfgNode, output, conditional);
        } else {
          /*
           * We want to traverse in reverse order because we want the LAST
           * definition in the sub-tree....
           * But we have no better way to traverse in reverse other :'(
           */
          for (Node c = n.getLastChild(); c != null; c = n.getChildBefore(c)) {
            computeMayUse(c, cfgNode, output, conditional);
          }
        }
    }
  }
