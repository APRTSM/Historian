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
            int start = 0;
          }
        }
      }

      // Make a copy of the Node, so it isn't changed by other inlines.
      fs.setSafeFnNode(fs.getFn().getFunctionNode().cloneTree());
    }
  }
