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
  void scanNewNodes(Scope scope, Set<Node> newNodes) {
    this.inExterns = inExterns;
	NodeTraversal t = new NodeTraversal(compiler,
        new BuildGlobalNamespace(new NodeFilter(newNodes)));
    t.traverseAtScope(scope);
  }
