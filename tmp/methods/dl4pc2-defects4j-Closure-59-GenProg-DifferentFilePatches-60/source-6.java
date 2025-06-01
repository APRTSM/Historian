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
            if (NodeUtil.isName(obj) && "arguments".equals(obj.getString())) {
              // TODO(user): More accuracy can be introduced
              // ie: We know exactly what arguments[x] is if x is a constant
              // number.
              escapeParameters(output);
            }
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
  public ReverseAbstractInterpreter getReverseAbstractInterpreter() {
    if (abstractInterpreter == null) {
      ChainableReverseAbstractInterpreter interpreter =
          new SemanticReverseAbstractInterpreter(
              getCodingConvention(), getTypeRegistry());
      if (options.closurePass) {
        interpreter = new ClosureReverseAbstractInterpreter(
            getCodingConvention(), getTypeRegistry())
            .append(interpreter).getFirst();
      }
      abstractInterpreter = interpreter;
    }
    return abstractInterpreter;
  }
  public void printSummary() {
    if (summaryDetailLevel >= 3 ||
        (summaryDetailLevel >= 1 && getErrorCount() + getWarningCount() > 0) ||
        (summaryDetailLevel >= 2 && getTypedPercent() > 0.0)) {
      if (getTypedPercent() > 0.0) {
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
  void setWarningLevel(CompilerOptions options,
      String name, CheckLevel level) {
    DiagnosticGroup group = forName(name);
    Preconditions.checkNotNull(group, "No warning class for name: " + name);
    options.setWarningLevel(group, level);
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
