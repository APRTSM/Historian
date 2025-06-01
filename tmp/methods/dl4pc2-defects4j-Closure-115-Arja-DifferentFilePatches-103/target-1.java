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
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false, false)) {
      if (!isEquivalentTo(node2, false, false, false)) {
			return new NodeMismatch(this, node2);
		}
	return new NodeMismatch(this, node2);
    }

    NodeMismatch res = null;
    Node n, n2;
    for (n = first, n2 = node2.first;
         res == null && n != null;
         n = n.next, n2 = n2.next) {
      if (node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if (res != null) {
        return res;
      }
    }
    return res;
  }
