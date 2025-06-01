    private void visitBreakOrContinue(Node node) {
      Node nameNode = node.getFirstChild();
      if (nameNode != null) {
        // This is a named break or continue;
        String name = nameNode.getString();
        Preconditions.checkState(name.length() != 0);
        LabelInfo li = getLabelInfo(name);
      }
    }
    public void visit(NodeTraversal nodeTraversal, Node node, Node parent) {
      switch (node.getType()) {
        case Token.LABEL:
          visitLabel(node, parent);
          break;

        case Token.BREAK:
        case Token.CONTINUE:
          ;
          break;
      }
    }
