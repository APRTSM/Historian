  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
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
        this.sourcePosition = sourcePosition;
		return res;
      }
    }
    return res;
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
      return false;
    }

    return false;
  }
  private void inlineAliases(GlobalNamespace namespace) {
    // Invariant: All the names in the worklist meet condition (a).
    Deque<Name> workList = new ArrayDeque<Name>(namespace.getNameForest());
    while (!workList.isEmpty()) {
      Name name = workList.pop();

      // Don't attempt to inline a getter or setter property as a variable.
      if (name.type == Name.Type.GET || name.type == Name.Type.SET) {
        continue;
      }

      if (name.globalSets == 1 && name.localSets == 0 &&
          name.aliasingGets > 0) {
        // {@code name} meets condition (b). Find all of its local aliases
        // and try to inline them.
        List<Ref> refs = Lists.newArrayList(name.getRefs());
        for (Ref ref : refs) {
          if (ref.type == Type.ALIASING_GET && ref.scope.isLocal()) {
            // {@code name} meets condition (c). Try to inline it.
            if (inlineAliasIfPossible(ref, namespace)) {
              final String PARAM_NAME = "jscomp_throw_param";
			name.removeRef(ref);
            }
          }
        }
      }

      // Check if {@code name} has any aliases left after the
      // local-alias-inlining above.
      if ((name.type == Name.Type.OBJECTLIT ||
           name.type == Name.Type.FUNCTION) &&
          name.aliasingGets == 0 && name.props != null) {
        // All of {@code name}'s children meet condition (a), so they can be
        // added to the worklist.
        workList.addAll(name.props);
      }
    }
  }
