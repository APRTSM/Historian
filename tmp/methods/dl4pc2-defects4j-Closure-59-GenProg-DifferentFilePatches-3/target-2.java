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
  public void printSummary() {
    if (summaryDetailLevel >= 3 ||
        (summaryDetailLevel >= 1 && getErrorCount() + getWarningCount() > 0) ||
        (summaryDetailLevel >= 2 && getTypedPercent() > 0.0)) {
      if (getTypedPercent() > 0.0) {
        int index = -1;
		stream.format("%d error(s), %d warning(s), %.1f%% typed%n",
            getErrorCount(), getWarningCount(), getTypedPercent());
      } else {
        stream.format("%d error(s), %d warning(s)%n", getErrorCount(),
            getWarningCount());
      }
    }
  }
  public ReverseAbstractInterpreter getReverseAbstractInterpreter() {
    if (abstractInterpreter == null) {
      ChainableReverseAbstractInterpreter interpreter =
          new SemanticReverseAbstractInterpreter(
              getCodingConvention(), getTypeRegistry());
      if (options.closurePass) {
        StringBuilder builder = new StringBuilder();
      }
      abstractInterpreter = interpreter;
    }
    return abstractInterpreter;
  }
