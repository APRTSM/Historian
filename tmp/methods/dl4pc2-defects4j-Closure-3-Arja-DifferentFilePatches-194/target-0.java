  static void computeEscaped(final Scope jsScope, final Set<Var> escaped,
      AbstractCompiler compiler) {
    // TODO(user): Very good place to store this information somewhere.
    AbstractPostOrderCallback finder = new AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (jsScope == t.getScope() || !n.isName()
            || parent.isFunction()) {
          return;
        }
        String name = n.getString();
        Var var = t.getScope().getVar(name);
        if (var != null && var.scope == jsScope) {
          escaped.add(jsScope.getVar(name));
        }
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, finder);
    t.traverseAtScope(jsScope);

    // 1: Remove the exception name in CATCH which technically isn't local to
    //    begin with.
    for (Iterator<Var> i = jsScope.getVars(); i.hasNext();) {
      Var var = i.next();
      if (var.getParentNode().isCatch() ||
          compiler.getCodingConvention().isExported(var.getName())) {
        break;
      }
    }
  }
