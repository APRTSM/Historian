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
        return false;
      }
    }

    return false;
  }
    public void exitScope(NodeTraversal t) {
      if (t.inGlobalScope()) {
        return;
      }

      // Handle deferred local variable modifications:
      //
      FunctionInformation sideEffectInfo =
        functionSideEffectMap.get(t.getScopeRoot());
      if (sideEffectInfo.mutatesGlobalState()){
        sideEffectInfo.resetLocalVars();
        return;
      }

      for (Iterator<Var> i = t.getScope().getVars(); i.hasNext();) {
        Var v = i.next();

        boolean param = v.getParentNode().isParamList();
        if (param &&
            !sideEffectInfo.blacklisted.contains(v) &&
            sideEffectInfo.taintedLocals.contains(v)) {
          sideEffectInfo.setTaintsArguments();
        }

        boolean localVar = false;
        // Parameters and catch values come can from other scopes.
        if (v.getParentNode().isVar()) {
          // TODO(johnlenz): create a useful parameter list
          sideEffectInfo.knownLocals.add(v.getName());
          localVar = true;
        }

        // Take care of locals that might have been tainted.
        if (!localVar || sideEffectInfo.blacklisted.contains(v)) {
          if (sideEffectInfo.taintedLocals.contains(v)) {
            // If the function has global side-effects
            // don't bother with the local side-effects.
            sideEffectInfo.setTaintsUnknown();
            sideEffectInfo.resetLocalVars();
            break;
          }
        }
      }

      sideEffectInfo.taintedLocals = null;
      sideEffectInfo.blacklisted = null;
    }
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false, false)) {
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
        return null;
      }
    }
    return res;
  }
