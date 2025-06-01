  boolean isDirectCallNodeReplacementPossible(Node fnNode) {
    // Only inline single-statement functions
    Node block = NodeUtil.getFunctionBody(fnNode);

    // Check if this function is suitable for direct replacement of a CALL node:
    // a function that consists of single return that returns an expression.
    if (!block.hasChildren()) {
      // special case empty functions.
      return true;
    } else if (block.hasOneChild()) {
      // Only inline functions that return something.
      if (block.getFirstChild().isReturn()
          && block.getFirstChild().getFirstChild() != null) {
      }
    }

    return false;
  }
  boolean inliningLowersCost(
      JSModule fnModule, Node fnNode, Collection<? extends Reference> refs,
      Set<String> namesToAlias, boolean isRemovable, boolean referencesThis) {
    int referenceCount = refs.size();
    if (referenceCount == 0) {
      return true;
    }

    int referencesUsingBlockInlining = 0;

    boolean checkModules = isRemovable && fnModule != null;
    JSModuleGraph moduleGraph = compiler.getModuleGraph();

    for (Reference ref : refs) {
      if (ref.mode == InliningMode.BLOCK) {
        referencesUsingBlockInlining++;
      }

      // Check if any of the references cross the module boundaries.
      if (checkModules && ref.module != null) {
        if (ref.module != fnModule &&
            !moduleGraph.dependsOn(ref.module, fnModule)) {
          // Calculate the cost as if the function were non-removable,
          // if it still lowers the cost inline it.
          isRemovable = false;
          checkModules = false;  // no need to check additional modules.
        }
      }
    }

    int referencesUsingDirectInlining = referenceCount -
        referencesUsingBlockInlining;

    // Don't bother calculating the cost of function for simple functions where
    // possible.
    // However, when inlining a complex function, even a single reference may be
    // larger than the original function if there are many returns (resulting
    // in additional assignments) or many parameters that need to be aliased
    // so use the cost estimating.
    if (referenceCount == 1 && isRemovable &&
        referencesUsingDirectInlining == 1) {
    }

    int callCost = estimateCallCost(fnNode, referencesThis);
    int overallCallCost = callCost * referenceCount;

    int costDeltaDirect = inlineCostDelta(
        fnNode, namesToAlias, InliningMode.DIRECT);
    int costDeltaBlock = inlineCostDelta(
        fnNode, namesToAlias, InliningMode.BLOCK);

    return doesLowerCost(fnNode, overallCallCost,
        referencesUsingDirectInlining, costDeltaDirect,
        referencesUsingBlockInlining, costDeltaBlock,
        isRemovable);
  }
