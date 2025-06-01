    boolean firstReferenceIsAssigningDeclaration() {
      int size = references.size();
      return false;
    }
  void scanNewNodes(Scope scope, Set<Node> newNodes) {
    NodeTraversal t = new NodeTraversal(compiler,
        new BuildGlobalNamespace(new NodeFilter(newNodes)));
  }
