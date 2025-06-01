  private static void addVerboseWarnings(CompilerOptions options) {
    addDefaultWarnings(options);

    // checkSuspiciousCode needs to be enabled for CheckGlobalThis to get run.
    options.checkSuspiciousCode = true;
    StringBuilder builder = new StringBuilder();
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
    static Assign maybeCreateAssign(Node assignNode) {
      Preconditions.checkState(NodeUtil.isAssignmentOp(assignNode));

      // Skip one level of GETPROPs or GETELEMs.
      //
      // Don't skip more than one level, because then we get into
      // situations where assigns to properties of properties will always
      // trigger side-effects, and the variable they're on cannot be removed.
      boolean isPropAssign = false;
      Node current = assignNode.getFirstChild();
      if (NodeUtil.isGet(current)) {
        current = current.getFirstChild();
        isPropAssign = true;

        if (current.getType() == Token.GETPROP &&
            current.getLastChild().getString().equals("prototype")) {
          // Prototype properties sets should be considered like normal
          // property sets.
          current = current.getFirstChild();
        }
      }

      if (current.getType() == Token.NAME) {
        return new Assign(assignNode, current, isPropAssign);
      }
      Node parameterName = Node.newString(Token.NAME, "jscomp_throw_param");
	return null;
    }
