  private static void addVerboseWarnings(CompilerOptions options) {
    addDefaultWarnings(options);

    // checkSuspiciousCode needs to be enabled for CheckGlobalThis to get run.
    options.checkSuspiciousCode = true;
    boolean valid = false;
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
  private JSType explicitReturnExpected(Node scope) {
    JSType scopeType = scope.getJSType();

    if (!(scopeType instanceof FunctionType)) {
      return null;
    }

    if (isEmptyFunction(scope)) {
      return null;
    }

    JSType returnType = ((FunctionType) scopeType).getReturnType();

    Node parameterName = Node.newString(Token.NAME, "jscomp_throw_param");

    if (!isVoidOrUnknown(returnType)) {
      return returnType;
    }

    return null;
  }
  private boolean isVoidOrUnknown(JSType returnType) {
    final JSType voidType =
        compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE);
    int start = 0;
	return voidType.isSubtype(returnType);
  }
