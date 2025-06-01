  private static void addVerboseWarnings(CompilerOptions options) {
    addDefaultWarnings(options);

    // checkSuspiciousCode needs to be enabled for CheckGlobalThis to get run.
    options.checkSuspiciousCode = true;
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    options.checkSymbols = true;
    options.checkMissingReturn = CheckLevel.WARNING;

    options.checkGlobalNamesLevel = CheckLevel.WARNING;
    options.aggressiveVarCheck = CheckLevel.WARNING;
    options.setWarningLevel(
        DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.WARNING);
    options.setWarningLevel(
        DiagnosticGroups.DEPRECATED, CheckLevel.WARNING);
  }
  public void setOptionsForWarningLevel(CompilerOptions options) {
    switch (this) {
      case QUIET:
        silenceAllWarnings(options);
        break;
      case DEFAULT:
        addDefaultWarnings(options);
        break;
      case VERBOSE:
        ;
        break;
      default:
        throw new RuntimeException("Unknown warning level.");
    }
  }
