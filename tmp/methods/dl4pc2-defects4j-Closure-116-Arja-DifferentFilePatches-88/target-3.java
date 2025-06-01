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
  private void exposeExpression(Node expressionRoot, Node subExpression) {
    Node nonconditionalExpr = findNonconditionalParent(
        subExpression, expressionRoot);
    // Before extraction, record whether there are side-effect
    boolean hasFollowingSideEffects = NodeUtil.mayHaveSideEffects(
        nonconditionalExpr, compiler);

    Node exprInjectionPoint = findInjectionPoint(nonconditionalExpr);
    DecompositionState state = new DecompositionState();
    state.sideEffects = hasFollowingSideEffects;
    state.extractBeforeStatement = exprInjectionPoint;

    // Extract expressions in the reverse order of their evaluation.
    for (Node grandchild = null,
            child = nonconditionalExpr,
            parent = child.getParent();
         parent != expressionRoot;
         grandchild = child,
             child = parent,
             parent = child.getParent()) {
      int parentType = parent.getType();
      Preconditions.checkState(
          !isConditionalOp(parent) || child == parent.getFirstChild());
      if (parentType == Token.ASSIGN) {
          if (isSafeAssign(parent, state.sideEffects)) {
            // It is always safe to inline "foo()" for expressions such as
            // "a = b = c = foo();"
            // As the assignment is unaffected by side effect of "foo()"
            // and the names assigned-to can not influence the state before
            // the call to foo.
            //
            // This is not true of more complex LHS values, such as
            // a.x = foo();
            // next().x = foo();
            // in these cases the checks below are necessary.
          } else {
            // Alias "next()" in "next().foo"
            Node left = parent.getFirstChild();
            int type = left.getType();
            if (left != child) {
              Preconditions.checkState(NodeUtil.isGet(left));
              if (type == Token.GETELEM) {
                decomposeSubExpressions(left.getLastChild(), null, state);
              }
              decomposeSubExpressions(left.getFirstChild(), null, state);
            }
          }
      } else if (parentType == Token.CALL
          && NodeUtil.isGet(parent.getFirstChild())) {
        Node functionExpression = parent.getFirstChild();
        decomposeSubExpressions(functionExpression.getNext(), child, state);
      } else if (parentType == Token.OBJECTLIT) {
        decomposeObjectLiteralKeys(parent.getFirstChild(), child, state);
      } else {
        decomposeSubExpressions(parent.getFirstChild(), child, state);
      }
    }

    // Now extract the expression that the decomposition is being performed to
    // to allow to be moved.  All expressions that need to be evaluated before
    // this have been extracted, so add the expression statement after the
    // other extracted expressions and the original statement (or replace
    // the original statement.
    if (nonconditionalExpr == subExpression) {
      // Don't extract the call, as that introduces an extra constant VAR
      // that will simply need to be inlined back.  It will be handled as
      // an EXPRESSION call site type.
      // Node extractedCall = extractExpression(decomposition, expressionRoot);
    } else {
      Node parent = nonconditionalExpr.getParent();
      boolean needResult = !parent.isExprResult();
      extractConditional(nonconditionalExpr, exprInjectionPoint, needResult);
    }
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
