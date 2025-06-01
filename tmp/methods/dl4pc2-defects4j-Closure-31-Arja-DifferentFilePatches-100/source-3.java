  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
      return new NodeMismatch(this, node2);
    }

    NodeMismatch res = null;
    Node n, n2;
    for (n = first, n2 = node2.first;
         res == null && n != null;
         n = n.next, n2 = n2.next) {
      if (node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if (res != null) {
        return res;
      }
    }
    return res;
  }
  public void skipAllCompilerPasses() {
    skipAllPasses = true;
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

    // Dependency options
    if (config.onlyClosureDependencies) {
      if (config.closureEntryPoints.isEmpty()) {
        throw new FlagUsageException("When only_closure_dependencies is "
          + "on, you must specify at least one closure_entry_point");
      }

      options.setDependencyOptions(new DependencyOptions()
          .setDependencyPruning(true)
          .setDependencySorting(true)
          .setMoocherDropping(true)
          .setEntryPoints(config.closureEntryPoints));
    } else if (config.manageClosureDependencies ||
        config.closureEntryPoints.size() > 0) {
      options.setDependencyOptions(new DependencyOptions()
          .setDependencyPruning(true)
          .setDependencySorting(true)
          .setMoocherDropping(false)
          .setEntryPoints(config.closureEntryPoints));
    }

    options.devMode = config.jscompDevMode;
    options.setCodingConvention(config.codingConvention);
    options.setSummaryDetailLevel(config.summaryDetailLevel);

    legacyOutputCharset = options.outputCharset = getLegacyOutputCharset();
    outputCharset2 = getOutputCharset2();
    inputCharset = getInputCharset();

    if (config.jsOutputFile.length() > 0) {
      if (config.skipNormalOutputs) {
        throw new FlagUsageException("skip_normal_outputs and js_output_file"
            + " cannot be used together.");
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

    if (!config.outputManifests.isEmpty()) {
      Set<String> uniqueNames = Sets.newHashSet();
      for (String filename : config.outputManifests) {
        if (!uniqueNames.add(filename)) {
          throw new FlagUsageException("output_manifest flags specify " +
              "duplicate file names: " + filename);
        }
      }
    }

    if (!config.outputBundles.isEmpty()) {
      Set<String> uniqueNames = Sets.newHashSet();
      for (String filename : config.outputBundles) {
        if (!uniqueNames.add(filename)) {
          throw new FlagUsageException("output_bundle flags specify " +
              "duplicate file names: " + filename);
        }
      }
    }

    options.acceptConstKeyword = config.acceptConstKeyword;
    options.transformAMDToCJSModules = config.transformAMDToCJSModules;
    options.processCommonJSModules = config.processCommonJSModules;
    options.commonJSModulePathPrefix = config.commonJSModulePathPrefix;
  }
  public void setOptionsForCompilationLevel(CompilerOptions options) {
    switch (this) {
      case WHITESPACE_ONLY:
        applyBasicCompilationOptions(options);
        break;
      case SIMPLE_OPTIMIZATIONS:
        applySafeCompilationOptions(options);
        break;
      case ADVANCED_OPTIMIZATIONS:
        applyFullCompilationOptions(options);
        break;
      default:
        throw new RuntimeException("Unknown compilation level.");
    }
  }
