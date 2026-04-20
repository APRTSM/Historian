    boolean firstReferenceIsAssigningDeclaration() {
      int size = references.size();
      return false;
    }
  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        Preconditions.checkState(this.propListHead == null,
				"Node has existing properties.");
		return "Node tree inequality:" +
            "\nTree1:\n" + toStringTree() +
            "\n\nTree2:\n" + node2.toStringTree() +
            "\n\nSubtree1: " + diff.nodeA.toStringTree() +
            "\n\nSubtree2: " + diff.nodeB.toStringTree();
      }
      return null;
  }
