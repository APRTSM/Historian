  private CanInlineResult canInlineReferenceDirectly(
      Node callNode, Node fnNode) {
    if (!isDirectCallNodeReplacementPossible(fnNode)) {
      return CanInlineResult.NO;
    }

    Node block = fnNode.getLastChild();

    boolean hasSideEffects = false;
    if (block.hasChildren()) {
      Preconditions.checkState(block.hasOneChild());
      Node stmt = block.getFirstChild();
      if (stmt.isReturn()) {
        hasSideEffects = NodeUtil.mayHaveSideEffects(stmt.getFirstChild(), compiler);
      }
    }
    // CALL NODE: [ NAME, ARG1, ARG2, ... ]
    Node cArg = callNode.getFirstChild().getNext();

    // Functions called via 'call' and 'apply' have a this-object as
    // the first parameter, but this is not part of the called function's
    // parameter list.
    if (!callNode.getFirstChild().isName()) {
      if (NodeUtil.isFunctionObjectCall(callNode)) {
        // TODO(johnlenz): Support replace this with a value.
        if (cArg == null || !cArg.isThis()) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      } else {
        // ".apply" call should be filtered before this.
        Preconditions.checkState(!NodeUtil.isFunctionObjectApply(callNode));
      }
    }

    // FUNCTION NODE -> LP NODE: [ ARG1, ARG2, ... ]
    Node fnParam = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    while (cArg != null || fnParam != null) {
      // For each named parameter check if a mutable argument use more than one.
      if (fnParam != null) {
        // Move to the next name.
        fnParam = fnParam.getNext();
      }

      // For every call argument check for side-effects, even if there
      // isn't a named parameter to match.
      if (cArg != null) {
        if (NodeUtil.mayHaveSideEffects(cArg, compiler)) {
          return CanInlineResult.NO;
        }
        cArg = cArg.getNext();
      }
    }

    return CanInlineResult.YES;
  }
  private boolean mimimizeCost(FunctionState fs) {
    if (!inliningLowersCost(fs)) {
      // Try again without Block inlining references
      if (fs.hasBlockInliningReferences()) {
        fs.setRemove(false);
        fs.removeBlockInliningReferences();
      } else {
        return false;
      }
    }
    return true;
  }
    void maybeAddReference(NodeTraversal t, FunctionState fs,
        Node callNode, JSModule module) {
      if (!fs.canInline()) {
        return;
      }

      boolean referenceAdded = false;
      InliningMode mode = fs.canInlineDirectly()
           ? InliningMode.DIRECT : InliningMode.BLOCK;
      referenceAdded = maybeAddReferenceUsingMode(
          t, fs, callNode, module, mode);
      if (!referenceAdded &&
          mode == InliningMode.DIRECT && blockFunctionInliningEnabled) {
        // This reference can not be directly inlined, see if
        // block replacement inlining is possible.
        mode = InliningMode.BLOCK;
      }

      if (!referenceAdded) {
        // Don't try to remove a function if we can't inline all
        // the references.
        fs.setRemove(false);
      }
    }
