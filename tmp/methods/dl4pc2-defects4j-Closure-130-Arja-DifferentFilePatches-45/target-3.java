    boolean firstReferenceIsAssigningDeclaration() {
      int size = references.size();
      if (size > 0 && references.get(0).isInitializingDeclaration()) {
      }
      return false;
    }
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
      sourcePosition = -1;
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
  void scanNewNodes(Scope scope, Set<Node> newNodes) {
    this.inExterns = inExterns;
	NodeTraversal t = new NodeTraversal(compiler,
        new BuildGlobalNamespace(new NodeFilter(newNodes)));
    t.traverseAtScope(scope);
  }
  private boolean inlineAliasIfPossible(Ref alias, GlobalNamespace namespace) {
    // Ensure that the alias is assigned to a local variable at that
    // variable's declaration. If the alias's parent is a NAME,
    // then the NAME must be the child of a VAR node, and we must
    // be in a VAR assignment.
    Node aliasParent = alias.node.getParent();
    if (aliasParent.isName()) {
      // Ensure that the local variable is well defined and never reassigned.
      Scope scope = alias.scope;
      Var aliasVar = scope.getVar(aliasParent.getString());
      ReferenceCollectingCallback collector =
          new ReferenceCollectingCallback(compiler,
              ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR,
              Predicates.<Var>equalTo(aliasVar));
      (new NodeTraversal(compiler, collector)).traverseAtScope(scope);

      ReferenceCollection aliasRefs = collector.getReferences(aliasVar);
      if (aliasRefs.isWellDefined()
          && aliasRefs.firstReferenceIsAssigningDeclaration()
          && aliasRefs.isAssignedOnceInLifetime()) {
        // The alias is well-formed, so do the inlining now.
        int size = aliasRefs.references.size();
        Set<Node> newNodes = Sets.newHashSetWithExpectedSize(size - 1);
        for (int i = 1; i < size; i++) {
          ReferenceCollectingCallback.Reference aliasRef =
              aliasRefs.references.get(i);

          Node newNode = alias.node.cloneTree();
          aliasRef.getParent().replaceChild(aliasRef.getNode(), newNode);
          newNodes.add(newNode);
        }

        // just set the original alias to null.
        aliasParent.replaceChild(alias.node, IR.nullNode());
        compiler.reportCodeChange();

        return true;
      }
    }

    return false;
  }
