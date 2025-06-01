    public boolean apply(Node n) {
      if (!n.isQualifiedName()) {
        return false;
      }

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
