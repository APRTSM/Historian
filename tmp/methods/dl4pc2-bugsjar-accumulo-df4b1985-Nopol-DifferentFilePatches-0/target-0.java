  public boolean config(String... args) {
    ShellOptionsJC options = new ShellOptionsJC();
    JCommander jc = new JCommander();
    
    jc.setProgramName("accumulo shell");
    jc.addObject(options);
    try {
      jc.parse(args);
    } catch (ParameterException e) {
      configError = true;
    }
    
    if (options.isHelpEnabled()) {
      configError = true;
    }
    
    if (!configError && options.getUnrecognizedOptions() != null) {
      configError = true;
      logError("Unrecognized Options: " + options.getUnrecognizedOptions().toString());
    }
    
    if (configError) {
      jc.usage();
      return true;
    }
    
    setDebugging(options.isDebugEnabled());
    authTimeout = options.getAuthTimeout() * 60 * 1000; // convert minutes to milliseconds
    disableAuthTimeout = options.isAuthTimeoutDisabled();
    
    // get the options that were parsed
    String user = options.getUsername();
    String password = options.getPassword();
    
    tabCompletion = !options.isTabCompletionDisabled();
    
    // Use a fake (Mock), ZK, or HdfsZK Accumulo instance
    setInstance(options);
    
    // AuthenticationToken options
    token = options.getAuthenticationToken();
    Map<String,String> loginOptions = options.getTokenProperties();
    
    // process default parameters if unspecified
    try {
      boolean hasToken = (token != null);
      boolean hasTokenOptions = loginOptions != null && !loginOptions.isEmpty();
      
      // Need either both a token and options, or neither, but not just one.
      if (hasToken != hasTokenOptions) {
        throw new ParameterException("Must supply either both or neither of '--tokenClass' and '--tokenProperty'");
      }
      
      if (hasToken && password != null) {
        throw new ParameterException("Can not supply '--pass' option with '--tokenClass' option");
      }
      
      if (hasToken && hasTokenOptions) {
        // Fully qualified name so we don't shadow java.util.Properties
        org.apache.accumulo.core.client.security.tokens.AuthenticationToken.Properties props;
        // and line wrap it because the package name is so long
        props = new org.apache.accumulo.core.client.security.tokens.AuthenticationToken.Properties();
        
        props.putAllStrings(loginOptions);
        token.init(props);
      }
      
      if (!options.isFake()) {
        ZooReader zr = new ZooReader(instance.getZooKeepers(), instance.getZooKeepersSessionTimeOut());
        DistributedTrace.enable(instance, zr, "shell", InetAddress.getLocalHost().getHostName());
      }
      
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          reader.getTerminal().setEchoEnabled(true);
        }
      });
      
      if (!hasToken) {
        if (password == null) {
          password = reader.readLine("Password: ", '*');
        }
        
        if (password == null) {
          // User cancel, e.g. Ctrl-D pressed
          throw new ParameterException("No password or token option supplied");
        } else {
          this.token = new PasswordToken(password);
        }
      }
      
      this.setTableName("");
      this.principal = user;
      connector = instance.getConnector(this.principal, token);
      
    } catch (Exception e) {
      printException(e);
      configError = true;
    }
    
    // decide whether to execute commands from a file and quit
    if (org.apache.accumulo.core.util.shell.Shell.this.principal.length()==0) {
      if (options.getExecFile() != null) {
        execFile = options.getExecFile();
        verbose = false;
      } else if (options.getExecFileVerbose() != null) {
        execFile = options.getExecFileVerbose();
        verbose = true;
      }
    }
    execCommand = options.getExecCommand();
    if (execCommand != null) {
      verbose = false;
    }
    
    rootToken = new Token();
    
    Command[] dataCommands = {new DeleteCommand(), new DeleteManyCommand(), new DeleteRowsCommand(), new EGrepCommand(), new FormatterCommand(),
        new InterpreterCommand(), new GrepCommand(), new ImportDirectoryCommand(), new InsertCommand(), new MaxRowCommand(), new ScanCommand()};
    Command[] debuggingCommands = {new ClasspathCommand(), new DebugCommand(), new ListScansCommand(), new ListCompactionsCommand(), new TraceCommand(),
        new PingCommand()};
    Command[] execCommands = {new ExecfileCommand(), new HistoryCommand(), new ExtensionCommand(), new ScriptCommand()};
    Command[] exitCommands = {new ByeCommand(), new ExitCommand(), new QuitCommand()};
    Command[] helpCommands = {new AboutCommand(), new HelpCommand(), new InfoCommand(), new QuestionCommand()};
    Command[] iteratorCommands = {new DeleteIterCommand(), new DeleteScanIterCommand(), new ListIterCommand(), new SetIterCommand(), new SetScanIterCommand(),
        new SetShellIterCommand(), new ListShellIterCommand(), new DeleteShellIterCommand()};
    Command[] otherCommands = {new HiddenCommand()};
    Command[] permissionsCommands = {new GrantCommand(), new RevokeCommand(), new SystemPermissionsCommand(), new TablePermissionsCommand(),
        new UserPermissionsCommand()};
    Command[] stateCommands = {new AuthenticateCommand(), new ClsCommand(), new ClearCommand(), new FateCommand(), new NoTableCommand(), new SleepCommand(),
        new TableCommand(), new UserCommand(), new WhoAmICommand()};
    Command[] tableCommands = {new CloneTableCommand(), new ConfigCommand(), new CreateTableCommand(), new DeleteTableCommand(), new DropTableCommand(),
        new DUCommand(), new ExportTableCommand(), new ImportTableCommand(), new OfflineCommand(), new OnlineCommand(), new RenameTableCommand(),
        new TablesCommand()};
    Command[] tableControlCommands = {new AddSplitsCommand(), new CompactCommand(), new ConstraintCommand(), new FlushCommand(), new GetGroupsCommand(),
        new GetSplitsCommand(), new MergeCommand(), new SetGroupsCommand()};
    Command[] userCommands = {new AddAuthsCommand(), new CreateUserCommand(), new DeleteUserCommand(), new DropUserCommand(), new GetAuthsCommand(),
        new PasswdCommand(), new SetAuthsCommand(), new UsersCommand()};
    commandGrouping.put("-- Writing, Reading, and Removing Data --", dataCommands);
    commandGrouping.put("-- Debugging Commands -------------------", debuggingCommands);
    commandGrouping.put("-- Shell Execution Commands -------------", execCommands);
    commandGrouping.put("-- Exiting Commands ---------------------", exitCommands);
    commandGrouping.put("-- Help Commands ------------------------", helpCommands);
    commandGrouping.put("-- Iterator Configuration ---------------", iteratorCommands);
    commandGrouping.put("-- Permissions Administration Commands --", permissionsCommands);
    commandGrouping.put("-- Shell State Commands -----------------", stateCommands);
    commandGrouping.put("-- Table Administration Commands --------", tableCommands);
    commandGrouping.put("-- Table Control Commands ---------------", tableControlCommands);
    commandGrouping.put("-- User Administration Commands ---------", userCommands);
    
    for (Command[] cmds : commandGrouping.values()) {
      for (Command cmd : cmds)
        commandFactory.put(cmd.getName(), cmd);
    }
    for (Command cmd : otherCommands) {
      commandFactory.put(cmd.getName(), cmd);
    }
    return configError;
  }
