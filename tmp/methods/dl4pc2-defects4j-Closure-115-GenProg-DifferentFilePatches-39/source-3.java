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
        if (cArg != null) {
          if (hasSideEffects && NodeUtil.canBeSideEffected(cArg)) {
            return CanInlineResult.NO;
          }
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
  private void resolveInlineConflictsForFunction(FunctionState fs) {
    // Functions that aren't referenced don't cause conflicts.
    if (!fs.hasReferences() || !fs.canInline()) {
      return;
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
        referenceAdded = maybeAddReferenceUsingMode(
            t, fs, callNode, module, mode);
      }

      if (!referenceAdded) {
        // Don't try to remove a function if we can't inline all
        // the references.
        fs.setRemove(false);
      }
    }
  private boolean mimimizeCost(FunctionState fs) {
    if (!inliningLowersCost(fs)) {
      // Try again without Block inlining references
      if (fs.hasBlockInliningReferences()) {
        fs.setRemove(false);
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
    private void visitBreakOrContinue(Node node) {
      Node nameNode = node.getFirstChild();
      if (nameNode != null) {
        // This is a named break or continue;
        String name = nameNode.getString();
        Preconditions.checkState(name.length() != 0);
        LabelInfo li = getLabelInfo(name);
        if (li != null) {
          String newName = getNameForId(li.id);
          // Mark the label as referenced so it isn't removed.
          li.referenced = true;
          if (!name.equals(newName)) {
            // Give it the short name.
            nameNode.setString(newName);
            compiler.reportCodeChange();
          }
        }
      }
    }
  static Node inject(AbstractCompiler compiler, Node node, Node parent,
      Map<String, Node> replacements, boolean replaceThis) {
    if (node.isName()) {
      Node replacementTemplate = replacements.get(node.getString());
      if (replacementTemplate != null) {
        // This should not be replacing declared names.
        Preconditions.checkState(!parent.isFunction()
            || !parent.isVar()
            || !parent.isCatch());
        // The name may need to be replaced more than once,
        // so we need to clone the node.
        Node replacement = replacementTemplate.cloneTree();
        parent.replaceChild(node, replacement);
        return replacement;
      }
    } else if (replaceThis && node.isThis()) {
      Node replacementTemplate = replacements.get(THIS_MARKER);
      Preconditions.checkNotNull(replacementTemplate);
      if (!replacementTemplate.isThis()) {
        // The name may need to be replaced more than once,
        // so we need to clone the node.
        Node replacement = replacementTemplate.cloneTree();
        parent.replaceChild(node, replacement);

        // Remove the value.  This isn't required but it ensures that we won't
        // inject side-effects multiple times as it will trigger the null
        // check above if we do.
        if (NodeUtil.mayHaveSideEffects(replacementTemplate, compiler)) {
          replacements.remove(THIS_MARKER);
        }

        return replacement;
      }
    } else if (node.isFunction()) {
      // Once we enter another scope the "this" value changes, don't try
      // to replace it within an inner scope.
      replaceThis = false;
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      // We have to reassign c in case it was replaced, because the removed c's
      // getNext() would no longer be correct.
      c = inject(compiler, c, node, replacements, replaceThis);
    }

    return node;
  }
