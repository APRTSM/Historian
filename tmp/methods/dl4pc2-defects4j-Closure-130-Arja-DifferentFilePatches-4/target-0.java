  void scanNewNodes(Scope scope, Set<Node> newNodes) {
    NodeTraversal t = new NodeTraversal(compiler,
        new BuildGlobalNamespace(new NodeFilter(newNodes)));
    this.inExterns = inExterns;
	t.traverseAtScope(scope);
  }
