  public void init(JSSourceFile[] externs, JSModule[] modules,
      CompilerOptions options) {
    initOptions(options);

    checkFirstModule(modules);
    fillEmptyModules(modules);

    this.externs = makeCompilerInput(externs, true);
    this.modules = modules;
    // Generate the module graph, and report any errors in the module
    // specification as errors.
    try {
      this.moduleGraph = new JSModuleGraph(modules);
    } catch (JSModuleGraph.ModuleDependenceException e) {
      // problems with the module format.  Report as an error.  The
      // message gives all details.
      report(JSError.make(MODULE_DEPENDENCY_ERROR,
          e.getModule().getName(), e.getDependentModule().getName()));
      return;
    }
    this.inputs = getAllInputsFromModules();
    initBasedOnOptions();

    initInputsByNameMap();
  }
  private void fillEmptyModules(JSModule[] modules) {
    for (int i = 1; i < modules.length; i ++) {
      JSModule module = modules[i];
      if (module.getInputs().isEmpty()) {
        module.add(JSSourceFile.fromCode("[" + module.getName() + "]", ""));
      }
    }
  }
  Node getNodeForCodeInsertion(JSModule module) {
    if (module == null) {
      if (inputs.length == 0) {
        throw new IllegalStateException("No inputs");
      }

      return inputs[0].getAstRoot(this);
    }

    List<CompilerInput> inputs = module.getInputs();
    if (inputs.size() > 0) {
      return inputs.get(0).getAstRoot(this);
    }
    throw new IllegalStateException("Root module has no inputs");
  }
