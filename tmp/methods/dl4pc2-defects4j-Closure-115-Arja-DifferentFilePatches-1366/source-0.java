    public void visit(NodeTraversal nodeTraversal, Node node, Node parent) {
      switch (node.getType()) {
        case Token.LABEL:
          visitLabel(node, parent);
          break;

        case Token.BREAK:
        case Token.CONTINUE:
          visitBreakOrContinue(node);
          break;
      }
    }
