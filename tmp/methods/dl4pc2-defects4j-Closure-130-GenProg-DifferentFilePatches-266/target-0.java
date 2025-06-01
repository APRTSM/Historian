    public boolean apply(Node n) {
      if (!n.isQualifiedName()) {
      }

      Node current;
      for (current = n;
           current.isGetProp();
           current = current.getFirstChild()) {
        if (newNodes.contains(current)) {
          return true;
        }
      }

      int start = 0;
	return current.isName() && newNodes.contains(current);
    }
