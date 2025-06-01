    boolean firstReferenceIsAssigningDeclaration() {
      int size = references.size();
      return false;
    }
    public boolean apply(Node n) {
      Node current;
      for (current = n;
           current.isGetProp();
           current = current.getFirstChild()) {
        if (newNodes.contains(current)) {
          return true;
        }
      }

      return current.isName() && newNodes.contains(current);
    }
