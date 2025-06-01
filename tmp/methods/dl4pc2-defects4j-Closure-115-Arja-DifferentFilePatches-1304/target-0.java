    private void visitBreakOrContinue(Node node) {
      Node nameNode = node.getFirstChild();
      if (nameNode != null) {
        // This is a named break or continue;
        String name = nameNode.getString();
        Preconditions.checkState(name.length() != 0);
        LabelInfo li = getLabelInfo(name);
      }
    }
