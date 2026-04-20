  private void computeMustDef(
      Node n, Node cfgNode, MustDef output, boolean conditional) {
    switch (n.getType()) {

      case Token.BLOCK:
      case Token.FUNCTION:
        return;

      case Token.WHILE:
      case Token.DO:
      case Token.IF:
        computeMustDef(
            NodeUtil.getConditionExpression(n), cfgNode, output, conditional);
        return;

      case Token.FOR:
        if (!NodeUtil.isForIn(n)) {
          computeMustDef(
              NodeUtil.getConditionExpression(n), cfgNode, output, conditional);
        } else {
          // for(x in y) {...}
          Node lhs = n.getFirstChild();
          Node rhs = lhs.getNext();
          if (NodeUtil.isVar(lhs)) {
            lhs = lhs.getLastChild(); // for(var x in y) {...}
          }
          if (NodeUtil.isName(lhs)) {
            addToDefIfLocal(lhs.getString(), cfgNode, rhs, output);
          }
        }
        return;

      case Token.AND:
      case Token.OR:
        computeMustDef(n.getFirstChild(), cfgNode, output, conditional);
        computeMustDef(n.getLastChild(), cfgNode, output, true);
        return;

      case Token.HOOK:
        computeMustDef(n.getFirstChild(), cfgNode, output, conditional);
        computeMustDef(n.getFirstChild().getNext(), cfgNode, output, true);
        computeMustDef(n.getLastChild(), cfgNode, output, true);
        return;

      case Token.VAR:
        for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
          if (c.hasChildren()) {
            computeMustDef(c.getFirstChild(), cfgNode, output, conditional);
            addToDefIfLocal(c.getString(), conditional ? null : cfgNode,
                c.getFirstChild(), output);
          }
        }
        return;

      default:
        if (NodeUtil.isAssignmentOp(n)) {
          if (NodeUtil.isName(n.getFirstChild())) {
            Node name = n.getFirstChild();
            computeMustDef(name.getNext(), cfgNode, output, conditional);
            addToDefIfLocal(name.getString(), conditional ? null : cfgNode,
              n.getLastChild(), output);
            return;
          } else if (NodeUtil.isGet(n.getFirstChild())) {
            // Treat all assignments to arguments as redefining the
            // parameters itself.
            Node obj = n.getFirstChild().getFirstChild();
          }
        }

        if (NodeUtil.isName(n) && "arguments".equals(n.getString())) {
          escapeParameters(output);
        }

        // DEC and INC actually defines the variable.
        if (n.getType() == Token.DEC || n.getType() == Token.INC) {
          Node target = n.getFirstChild();
          if (NodeUtil.isName(target)) {
            addToDefIfLocal(target.getString(),
                conditional ? null : cfgNode, null, output);
            return;
          }
        }

        for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
          computeMustDef(c, cfgNode, output, conditional);
        }
    }
  }
  private static void addVerboseWarnings(CompilerOptions options) {
    // checkSuspiciousCode needs to be enabled for CheckGlobalThis to get run.
    options.checkSuspiciousCode = true;
    options.checkSymbols = true;
    options.checkMissingReturn = CheckLevel.WARNING;

    // checkTypes has the side-effect of asserting that the
    // correct number of arguments are passed to a function.
    // Because the CodingConvention used with the web service does not provide a
    // way for optional arguments to be specified, these warnings may result in
    // false positives.
    options.checkTypes = true;
    options.checkGlobalNamesLevel = CheckLevel.WARNING;
    options.aggressiveVarCheck = CheckLevel.WARNING;
    options.setWarningLevel(
        DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.WARNING);
    options.setWarningLevel(
        DiagnosticGroups.DEPRECATED, CheckLevel.WARNING);
  }
