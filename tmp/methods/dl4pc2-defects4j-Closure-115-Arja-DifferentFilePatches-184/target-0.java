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
