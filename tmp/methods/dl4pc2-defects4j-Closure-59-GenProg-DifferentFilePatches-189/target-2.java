  private static void addVerboseWarnings(CompilerOptions options) {
    addDefaultWarnings(options);

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
