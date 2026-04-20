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
    private void visitBreakOrContinue(Node node) {
      Node nameNode = node.getFirstChild();
      if (nameNode != null) {
        // This is a named break or continue;
        String name = nameNode.getString();
        LabelInfo li = getLabelInfo(name);
        if (li != null) {
          String newName = getNameForId(li.id);
          // Mark the label as referenced so it isn't removed.
          li.referenced = true;
          if (!name.equals(newName)) {
            // Give it the short name.
            nameNode.setString(newName);
            compiler.reportCodeChange();
          }
        }
      }
    }
