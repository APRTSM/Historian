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
  private JSType explicitReturnExpected(Node scope) {
    JSType scopeType = scope.getJSType();

    if (!(scopeType instanceof FunctionType)) {
      return null;
    }

    JSType returnType = ((FunctionType) scopeType).getReturnType();

    if (returnType == null) {
      return null;
    }

    if (!isVoidOrUnknown(returnType)) {
      return returnType;
    }

    return null;
  }
  protected void setRunOptions(CompilerOptions options)
      throws FlagUsageException, IOException {
    DiagnosticGroups diagnosticGroups = getDiagnosticGroups();

    if (config.warningGuards != null) {
      for (WarningGuardSpec.Entry entry : config.warningGuards.entries) {
      }
    }

    createDefineOrTweakReplacements(config.define, options, false);

    options.setTweakProcessing(config.tweakProcessing);
    createDefineOrTweakReplacements(config.tweak, options, true);

    options.manageClosureDependencies = config.manageClosureDependencies;
    if (config.closureEntryPoints.size() > 0) {
      options.setManageClosureDependencies(config.closureEntryPoints);
    }
    options.devMode = config.jscompDevMode;
    options.setCodingConvention(config.codingConvention);
    options.setSummaryDetailLevel(config.summaryDetailLevel);

    outputCharset = options.outputCharset = getOutputCharset();
    inputCharset = getInputCharset();

    if (config.jsOutputFile.length() > 0) {
      if (config.skipNormalOutputs) {
        throw new FlagUsageException("skip_normal_outputs and js_output_file"
            + " cannot be used together.");
      } else {
        options.jsOutputFile = config.jsOutputFile;
      }
    }

    if (config.skipNormalOutputs && config.printAst) {
      throw new FlagUsageException("skip_normal_outputs and print_ast cannot"
          + " be used together.");
    }

    if (config.skipNormalOutputs && config.printTree) {
      throw new FlagUsageException("skip_normal_outputs and print_tree cannot"
          + " be used together.");
    }

    if (config.createSourceMap.length() > 0) {
      options.sourceMapOutputPath = config.createSourceMap;
    }
    options.sourceMapDetailLevel = config.sourceMapDetailLevel;
    options.sourceMapFormat = config.sourceMapFormat;

    if (!config.variableMapInputFile.equals("")) {
      options.inputVariableMapSerialized =
          VariableMap.load(config.variableMapInputFile).toBytes();
    }

    if (!config.propertyMapInputFile.equals("")) {
      options.inputPropertyMapSerialized =
          VariableMap.load(config.propertyMapInputFile).toBytes();
    }

    if (config.languageIn.length() > 0) {
      if (config.languageIn.equals("ECMASCRIPT5_STRICT") ||
          config.languageIn.equals("ES5_STRICT")) {
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
      } else if (config.languageIn.equals("ECMASCRIPT5") ||
          config.languageIn.equals("ES5")) {
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
      } else if (config.languageIn.equals("ECMASCRIPT3") ||
                 config.languageIn.equals("ES3")) {
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
      } else {
        throw new FlagUsageException("Unknown language `" + config.languageIn +
                                     "' specified.");
      }
    }

    options.acceptConstKeyword = config.acceptConstKeyword;
  }
  protected List<PassFactory> getChecks() {
    List<PassFactory> checks = Lists.newArrayList();

    if (options.closurePass) {
      checks.add(closureGoogScopeAliases);
    }

    if (options.nameAnonymousFunctionsOnly) {
      if (options.anonymousFunctionNaming ==
          AnonymousFunctionNamingPolicy.MAPPED) {
        checks.add(nameMappedAnonymousFunctions);
      } else if (options.anonymousFunctionNaming ==
          AnonymousFunctionNamingPolicy.UNMAPPED) {
        checks.add(nameUnmappedAnonymousFunctions);
      }
      return checks;
    }

    if (options.checkSuspiciousCode ||
        options.enables(DiagnosticGroups.GLOBAL_THIS)) {
      checks.add(suspiciousCode);
    }

    if (options.checkControlStructures)  {
      checks.add(checkControlStructures);
    }

    if (options.checkRequires.isOn()) {
      checks.add(checkRequires);
    }

    if (options.checkProvides.isOn()) {
      checks.add(checkProvides);
    }

    // The following passes are more like "preprocessor" passes.
    // It's important that they run before most checking passes.
    // Perhaps this method should be renamed?
    if (options.generateExports) {
      checks.add(generateExports);
    }

    if (options.exportTestFunctions) {
      checks.add(exportTestFunctions);
    }

    if (options.closurePass) {
      checks.add(closurePrimitives.makeOneTimePass());
    }

    if (options.closurePass && options.checkMissingGetCssNameLevel.isOn()) {
      checks.add(closureCheckGetCssName);
    }

    if (options.syntheticBlockStartMarker != null) {
      // This pass must run before the first fold constants pass.
      checks.add(createSyntheticBlocks);
    }

    checks.add(checkVars);
    if (options.computeFunctionSideEffects) {
      checks.add(checkRegExp);
    }

    if (options.checkShadowVars.isOn()) {
      checks.add(checkShadowVars);
    }

    if (options.aggressiveVarCheck.isOn()) {
    }

    // This pass should run before types are assigned.
    if (options.processObjectPropertyString) {
      checks.add(objectPropertyStringPreprocess);
    }

    if (options.checkTypes || options.inferTypes) {
      checks.add(resolveTypes.makeOneTimePass());
      checks.add(inferTypes.makeOneTimePass());
      if (options.checkTypes) {
        checks.add(checkTypes.makeOneTimePass());
      } else {
        checks.add(inferJsDocInfo.makeOneTimePass());
      }
    }

    if (options.checkUnreachableCode.isOn() ||
        (options.checkTypes && options.checkMissingReturn.isOn())) {
      checks.add(checkControlFlow);
    }

    // CheckAccessControls only works if check types is on.
    if (options.checkTypes &&
        (options.enables(DiagnosticGroups.ACCESS_CONTROLS)
         || options.enables(DiagnosticGroups.CONSTANT_PROPERTY))) {
      checks.add(checkAccessControls);
    }

    if (options.checkGlobalNamesLevel.isOn()) {
      checks.add(checkGlobalNames);
    }

    checks.add(checkStrictMode);

    // Replace 'goog.getCssName' before processing defines but after the
    // other checks have been done.
    if (options.closurePass) {
      checks.add(closureReplaceGetCssName);
    }

    // i18n
    // If you want to customize the compiler to use a different i18n pass,
    // you can create a PassConfig that calls replacePassFactory
    // to replace this.
    checks.add(options.messageBundle != null ?
        replaceMessages : createEmptyPass("replaceMessages"));

    if (options.getTweakProcessing().isOn()) {
      checks.add(processTweaks);
    }

    // Defines in code always need to be processed.
    checks.add(processDefines);

    if (options.instrumentationTemplate != null ||
        options.recordFunctionInformation) {
      checks.add(computeFunctionNames);
    }

    if (options.nameReferenceGraphPath != null &&
        !options.nameReferenceGraphPath.isEmpty()) {
      checks.add(printNameReferenceGraph);
    }

    if (options.nameReferenceReportPath != null &&
        !options.nameReferenceReportPath.isEmpty()) {
      checks.add(printNameReferenceReport);
    }

    assertAllOneTimePasses(checks);
    return checks;
  }
