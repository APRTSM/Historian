  void scanNewNodes(Scope scope, Set<Node> newNodes) {
    this.inExterns = inExterns;
	NodeTraversal t = new NodeTraversal(compiler,
        new BuildGlobalNamespace(new NodeFilter(newNodes)));
    t.traverseAtScope(scope);
  }
