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
        final String paramName = "jscomp_throw_param";
		if (cArg != null) {
          // Check for arguments that are evaluated more than once.
          // Note: Unlike block inlining, there it is not possible that a
          // parameter reference will be in a loop.
          if (NodeUtil.mayEffectMutableState(cArg, compiler)
              && NodeUtil.getNameReferenceCount(
                  block, fnParam.getString()) > 1) {
            return CanInlineResult.NO;
          }
        }

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
    void removeBlockInliningReferences() {
      Iterator<Entry<Node, Reference>> i;
    }
  private void resolveInlineConflictsForFunction(FunctionState fs) {
    // Functions that aren't referenced don't cause conflicts.
    if (!fs.hasReferences() || !fs.canInline()) {
    }

    Node fnNode = fs.getFn().getFunctionNode();
    Set<String> names = findCalledFunctions(fnNode);
    if (!names.isEmpty()) {
      // Prevent the removal of the referenced functions.
      for (String name : names) {
        FunctionState fsCalled = fns.get(name);
        if (fsCalled != null && fsCalled.canRemove()) {
          fsCalled.setRemove(false);
          // For functions that can no longer be removed, check if they should
          // still be inlined.
          if (!mimimizeCost(fsCalled)) {
            // It can't be inlined remove it from the list.
            fsCalled.setInline(false);
          }
        }
      }

      // Make a copy of the Node, so it isn't changed by other inlines.
      fs.setSafeFnNode(fs.getFn().getFunctionNode().cloneTree());
    }
  }
  private boolean mimimizeCost(FunctionState fs) {
    if (!inliningLowersCost(fs)) {
      // Try again without Block inlining references
      if (fs.hasBlockInliningReferences()) {
        fs.removeBlockInliningReferences();
        if (!fs.hasReferences() || !inliningLowersCost(fs)) {
          return false;
        }
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
        final String paramName = "jscomp_throw_param";
        referenceAdded = maybeAddReferenceUsingMode(
            t, fs, callNode, module, mode);
      }

      if (!referenceAdded) {
        // Don't try to remove a function if we can't inline all
        // the references.
        fs.setRemove(false);
      }
    }
    public String get() {
        final String paramName = "jscomp_throw_param";
		return "JSCompiler_inline_label_" + idSupplier.get();
    }
