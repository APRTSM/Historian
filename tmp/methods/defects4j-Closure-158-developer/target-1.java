    protected void clear() {
      entries.clear();
    }
    CommandLineConfig setWarningGuardSpec(WarningGuardSpec spec) {
      this.warningGuards = spec;
      return this;
    }
    protected void add(CheckLevel level, String groupName) {
      entries.add(new Entry(level, groupName));
    }
  final protected void setRunOptions(CompilerOptions options)
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
      options.jsOutputFile = config.jsOutputFile;
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
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
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
      @Override public void addValue(Object value) throws CmdLineException {
        proxy.addValue((String) value);
        warningGuardSpec.add(level, (String) value);
      }
      @Override public boolean isMultiValued() { return proxy.isMultiValued(); }
      @Override public Class getType() { return proxy.getType(); }
  private void initConfigFromFlags(String[] args, PrintStream err) {

    List<String> processedArgs = processArgs(args);

    CmdLineParser parser = new CmdLineParser(flags);
    Flags.warningGuardSpec.clear();
    isConfigValid = true;
    try {
      parser.parseArgument(processedArgs.toArray(new String[] {}));
      // For contains --flagfile flag
      if (!flags.flag_file.equals("")) {
        processFlagFile(err);
      }
    } catch (CmdLineException e) {
      err.println(e.getMessage());
      isConfigValid = false;
    } catch (IOException ioErr) {
      err.println("ERROR - " + flags.flag_file + " read error.");
      isConfigValid = false;
    }

    if (flags.version) {
      ResourceBundle config = ResourceBundle.getBundle(configResource);
      err.println(
          "Closure Compiler (http://code.google.com/closure/compiler)\n" +
          "Version: " + config.getString("compiler.version") + "\n" +
          "Built on: " + config.getString("compiler.date"));
      err.flush();
    }

    if (!isConfigValid || flags.display_help) {
      isConfigValid = false;
      parser.printUsage(err);
    } else {
      getCommandLineConfig()
          .setPrintTree(flags.print_tree)
          .setComputePhaseOrdering(flags.compute_phase_ordering)
          .setPrintAst(flags.print_ast)
          .setPrintPassGraph(flags.print_pass_graph)
          .setJscompDevMode(flags.jscomp_dev_mode)
          .setLoggingLevel(flags.logging_level)
          .setExterns(flags.externs)
          .setJs(flags.js)
          .setJsOutputFile(flags.js_output_file)
          .setModule(flags.module)
          .setVariableMapInputFile(flags.variable_map_input_file)
          .setPropertyMapInputFile(flags.property_map_input_file)
          .setVariableMapOutputFile(flags.variable_map_output_file)
          .setCreateNameMapFiles(flags.create_name_map_files)
          .setPropertyMapOutputFile(flags.property_map_output_file)
          .setCodingConvention(flags.third_party ?
               new DefaultCodingConvention() :
               new ClosureCodingConvention())
          .setSummaryDetailLevel(flags.summary_detail_level)
          .setOutputWrapper(flags.output_wrapper)
          .setModuleWrapper(flags.module_wrapper)
          .setModuleOutputPathPrefix(flags.module_output_path_prefix)
          .setCreateSourceMap(flags.create_source_map)
          .setWarningGuardSpec(Flags.warningGuardSpec)
          .setDefine(flags.define)
          .setCharset(flags.charset)
          .setManageClosureDependencies(flags.manage_closure_dependencies)
          .setClosureEntryPoints(flags.closure_entry_point)
          .setOutputManifest(flags.output_manifest)
          .setAcceptConstKeyword(flags.accept_const_keyword)
          .setLanguageIn(flags.language_in);
    }
  }
  private void processFlagFile(PrintStream err)
            throws CmdLineException, IOException {
    List<String> argsInFile = Lists.newArrayList();
    File flagFileInput = new File(flags.flag_file);
    StringTokenizer tokenizer = new StringTokenizer(
        Files.toString(flagFileInput, Charset.defaultCharset()));

    while (tokenizer.hasMoreTokens()) {
        argsInFile.add(tokenizer.nextToken());
    }

    flags.flag_file = "";
    List<String> processedFileArgs
        = processArgs(argsInFile.toArray(new String[] {}));
    CmdLineParser parserFileArgs = new CmdLineParser(flags);
    Flags.warningGuardSpec.clear();
    parserFileArgs.parseArgument(processedFileArgs.toArray(new String[] {}));

    // Currently we are not supporting this (prevent direct/indirect loops)
    if (!flags.flag_file.equals("")) {
      err.println("ERROR - Arguments in the file cannot contain "
          + "--flagfile option.");
      isConfigValid = false;
    }
  }
