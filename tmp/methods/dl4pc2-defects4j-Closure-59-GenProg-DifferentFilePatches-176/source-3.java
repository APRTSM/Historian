  private static void addVerboseWarnings(CompilerOptions options) {
    addDefaultWarnings(options);

    // checkSuspiciousCode needs to be enabled for CheckGlobalThis to get run.
    options.checkSuspiciousCode = true;
    options.checkGlobalThisLevel = CheckLevel.WARNING;
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
        diagnosticGroups.setWarningLevel(options, entry.groupName, entry.level);
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
      return null;
    }
