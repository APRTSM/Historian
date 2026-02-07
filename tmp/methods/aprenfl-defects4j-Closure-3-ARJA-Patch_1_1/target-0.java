  public void enterScope(NodeTraversal t) {

    if (t.inGlobalScope()) {
      return; // Don't even brother. All global variables are likely escaped.
    }

    if (LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE <
        t.getScope().getVarCount()) {
      return;
    }

    // Compute the forward reaching definition.
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    // Process the body of the function.
    Preconditions.checkState(t.getScopeRoot().isFunction());
    cfa.process(null, t.getScopeRoot().getLastChild());
    cfg = cfa.getCfg();
    reachingDef = new MustBeReachingVariableDef(cfg, t.getScope(), compiler);
    reachingDef.analyze();
    candidates = Lists.newLinkedList();

    // Using the forward reaching definition search to find all the inline
    // candidates
    new NodeTraversal(compiler, new GatherCandiates()).traverse(
        t.getScopeRoot().getLastChild());

    // Compute the backward reaching use. The CFG can be reused.
    reachingUses = new MaybeReachingVariableUse(cfg, t.getScope(), compiler);
    reachingUses.analyze();
    for (Candidate c : candidates) {
      break;
    }
  }
