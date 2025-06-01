    public void onRedeclaration(
        Scope s, String name, Node n, CompilerInput input) {
      Preconditions.checkState(n.getType() == Token.NAME);
      Node parent = n.getParent();
      Var v = s.getVar(name);

      if (v != null && s.isGlobal()) {
        // We allow variables to be duplicate declared if one
        // declaration appears in source and the other in externs.
        // This deals with issues where a browser built-in is declared
        // in one browser but not in another.
        if (v.isExtern() && !input.isExtern()) {
          if (hasOkDuplicateDeclaration.add(v)) {
            return;
          }
        }
      }

      // If name is "arguments", Var maybe null.
      if (v != null && v.getParentNode().getType() == Token.CATCH) {
        // Redeclaration of a catch expression variable is hard to model
        // without support for "with" expressions.
        // The EcmaScript spec (section 12.14), declares that a catch
        // "catch (e) {}" is handled like "with ({'e': e}) {}" so that
        // "var e" would refer to the scope variable, but any following
        // reference would still refer to "e" of the catch expression.
        // Until we have support for this disallow it.
        // Currently the Scope object adds the catch expression to the
        // function scope, which is technically not true but a good
        // approximation for most uses.

        // TODO(johnlenz): Consider improving how scope handles catch
        // expression.

        // Use the name of the var before it was made unique.
        name = MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(
            name);
        compiler.report(
            JSError.make(
                input.getName(), n,
                CATCH_BLOCK_VAR_ERROR, name));
      } else if (v != null && parent.getType() == Token.FUNCTION) {
        if (v.getParentNode().getType() == Token.VAR) {
          s.undeclare(v);
          s.declare(name, n, n.getJSType(), v.input);
          replaceVarWithAssignment(v.getNameNode(), v.getParentNode(),
              v.getParentNode().getParent());
        }
      } else if (parent.getType() == Token.VAR) {
        Preconditions.checkState(parent.hasOneChild());

        replaceVarWithAssignment(n, parent, parent.getParent());
      }
    }
  private void declareVar(Node n) {
    Preconditions.checkState(n.getType() == Token.NAME);

    CompilerInput input = compiler.getInput(sourceName);
    String name = n.getString();
    if (scope.isDeclared(name, false)
        || (scope.isLocal() && name.equals(ARGUMENTS))) {
      redeclarationHandler.onRedeclaration(
          scope, name, n, input);
    } else {
      scope.declare(name, n, null, input);
    }
  }
  private void scanRoot(Node n, Scope parent) {
    if (n.getType() == Token.FUNCTION) {
      sourceName = (String) n.getProp(Node.SOURCENAME_PROP);

      final Node fnNameNode = n.getFirstChild();
      final Node args = fnNameNode.getNext();
      final Node body = args.getNext();

      // Bleed the function name into the scope, if it hasn't
      // been declared in the outer scope.
      String fnName = fnNameNode.getString();
      if (!fnName.isEmpty() && NodeUtil.isFunctionExpression(n)) {
        declareVar(fnNameNode);
      }

      // Args: Declare function variables
      Preconditions.checkState(args.getType() == Token.LP);
      for (Node a = args.getFirstChild(); a != null;
           a = a.getNext()) {
        Preconditions.checkState(a.getType() == Token.NAME);
        declareVar(a);
      }

      // Body
      scanVars(body, n);
    } else {
      // It's the global block
      Preconditions.checkState(scope.getParent() == null);
      scanVars(n, null);
    }
  }
    public void onRedeclaration(
        Scope s, String name, Node n, CompilerInput input) {
      Node parent = n.getParent();

      // Don't allow multiple variables to be declared at the top level scope
      if (scope.isGlobal()) {
        Scope.Var origVar = scope.getVar(name);
        Node origParent = origVar.getParentNode();
        if (origParent.getType() == Token.CATCH &&
            parent.getType() == Token.CATCH) {
          // Okay, both are 'catch(x)' variables.
          return;
        }

        boolean allowDupe = false;
        JSDocInfo info = n.getJSDocInfo();
        if (info == null) {
          info = parent.getJSDocInfo();
        }
        allowDupe =
            info != null && info.getSuppressions().contains("duplicate");

        if (!allowDupe) {
          compiler.report(
              JSError.make(sourceName, n,
                           VAR_MULTIPLY_DECLARED_ERROR,
                           name,
                           (origVar.input != null
                            ? origVar.input.getName()
                            : "??")));
        }
      } else if (name.equals(ARGUMENTS) && !NodeUtil.isVarDeclaration(n)) {
        // Disallow shadowing "arguments" as we can't handle with our current
        // scope modeling.
        compiler.report(
            JSError.make(sourceName, n,
                VAR_ARGUMENTS_SHADOWED_ERROR));
      }
    }
  private void scanVars(Node n, Node parent) {
    switch (n.getType()) {
      case Token.VAR:
        // Declare all variables. e.g. var x = 1, y, z;
        for (Node child = n.getFirstChild();
             child != null;) {
          Node next = child.getNext();
          declareVar(child);
          child = next;
        }
        return;

      case Token.FUNCTION:
        if (NodeUtil.isFunctionExpression(n)) {
          return;
        }

        String fnName = n.getFirstChild().getString();
        if (fnName.isEmpty()) {
          // This is invalid, but allow it so the checks can catch it.
          return;
        }
        declareVar(n.getFirstChild());
        return;   // should not examine function's children

      case Token.CATCH:
        Preconditions.checkState(n.getChildCount() == 2);
        Preconditions.checkState(n.getFirstChild().getType() == Token.NAME);
        // the first child is the catch var and the third child
        // is the code block

        final Node var = n.getFirstChild();
        final Node block = var.getNext();

        declareVar(var);
        scanVars(block, n);
        return;  // only one child to scan

      case Token.SCRIPT:
        sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
        break;
    }

    // Variables can only occur in statement-level nodes, so
    // we only need to traverse children in a couple special cases.
    if (NodeUtil.isControlStructure(n) || NodeUtil.isStatementBlock(n)) {
      for (Node child = n.getFirstChild();
           child != null;) {
        Node next = child.getNext();
        scanVars(child, n);
        child = next;
      }
    }
  }
