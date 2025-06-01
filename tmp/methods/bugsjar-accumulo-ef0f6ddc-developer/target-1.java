  public int start() throws IOException {
    if (configError)
      return 1;
    
    String input;
    if (isVerbose())
      printInfo();
    
    if (execFile != null) {
      java.util.Scanner scanner = new java.util.Scanner(new File(execFile));
      try {
        while (scanner.hasNextLine() && !hasExited()) {
          execCommand(scanner.nextLine(), true, isVerbose());
        }
      } finally {
        scanner.close();
      }
    } else if (execCommand != null) {
      for (String command : execCommand.split("\n")) {
        execCommand(command, true, isVerbose());
      }
      return exitCode;
    }
    
    while (true) {
      if (hasExited())
        return exitCode;
      
      reader.setDefaultPrompt(getDefaultPrompt());
      input = reader.readLine();
      if (input == null) {
        reader.printNewline();
        return exitCode;
      } // user canceled
      
      execCommand(input, false, false);
    }
  }
  public int start() throws IOException {
    if (configError)
      return 1;
    
    String input;
    if (isVerbose())
      printInfo();
    
    String home = System.getProperty("HOME");
    if (home == null)
      home = System.getenv("HOME");
    String configDir = home + "/.accumulo";
    String historyPath = configDir + "/shell_history.txt";
    File accumuloDir = new File(configDir);
    if (!accumuloDir.exists() && !accumuloDir.mkdirs())
      log.warn("Unable to make directory for history at " + accumuloDir);
    try {
      History history = new History();
      history.setHistoryFile(new File(historyPath));
      reader.setHistory(history);
    } catch (IOException e) {
      log.warn("Unable to load history file at " + historyPath);
    }
    
    ShellCompletor userCompletor = null;
    
    if (execFile != null) {
      java.util.Scanner scanner = new java.util.Scanner(new File(execFile));
      try {
        while (scanner.hasNextLine() && !hasExited()) {
          execCommand(scanner.nextLine(), true, isVerbose());
        }
      } finally {
        scanner.close();
      }
    } else if (execCommand != null) {
      for (String command : execCommand.split("\n")) {
        execCommand(command, true, isVerbose());
      }
      return exitCode;
    }
    
    while (true) {
      if (hasExited())
        return exitCode;
      
      // If tab completion is true we need to reset
      if (tabCompletion) {
        if (userCompletor != null)
          reader.removeCompletor(userCompletor);
        
        userCompletor = setupCompletion();
        reader.addCompletor(userCompletor);
      }
      
      reader.setDefaultPrompt(getDefaultPrompt());
      input = reader.readLine();
      if (input == null) {
        reader.printNewline();
        return exitCode;
      } // user canceled
      
      execCommand(input, disableAuthTimeout, false);
    }
  }
  public boolean config(String... args) {
    
    CommandLine cl;
    try {
      cl = new BasicParser().parse(opts, args);
      if (cl.getArgs().length > 0)
        throw new ParseException("Unrecognized arguments: " + cl.getArgList());
      
      if (cl.hasOption(helpOpt.getOpt())) {
        configError = true;
        printHelp("shell", SHELL_DESCRIPTION, opts);
        return true;
      }
      
      setDebugging(cl.hasOption(debugOption.getLongOpt()));
      authTimeout = Integer.parseInt(cl.getOptionValue(authTimeoutOpt.getLongOpt(), DEFAULT_AUTH_TIMEOUT)) * 60 * 1000;
      disableAuthTimeout = cl.hasOption(disableAuthTimeoutOpt.getLongOpt());
      
      if (cl.hasOption(zooKeeperInstance.getOpt()) && cl.getOptionValues(zooKeeperInstance.getOpt()).length != 2)
        throw new MissingArgumentException(zooKeeperInstance);
      
    } catch (Exception e) {
      configError = true;
      printException(e);
      printHelp("shell", SHELL_DESCRIPTION, opts);
      return true;
    }
    
    // get the options that were parsed
    String sysUser = System.getProperty("user.name");
    if (sysUser == null)
      sysUser = "root";
    String user = cl.getOptionValue(usernameOption.getOpt(), sysUser);

    String passw = cl.getOptionValue(passwOption.getOpt(), null);
    tabCompletion = !cl.hasOption(tabCompleteOption.getLongOpt());
    String[] loginOptions = cl.getOptionValues(loginOption.getOpt());
    
    // Use a fake (Mock), ZK, or HdfsZK Accumulo instance
    setInstance(cl);
    
    // process default parameters if unspecified
    try {
      if (loginOptions != null && !cl.hasOption(tokenOption.getOpt()))
        throw new IllegalArgumentException("Must supply '-" + tokenOption.getOpt() + "' option with '-" + loginOption.getOpt() + "' option");
      
      if (loginOptions == null && cl.hasOption(tokenOption.getOpt()))
        throw new IllegalArgumentException("Must supply '-" + loginOption.getOpt() + "' option with '-" + tokenOption.getOpt() + "' option");

      if (passw != null && cl.hasOption(tokenOption.getOpt()))
        throw new IllegalArgumentException("Can not supply '-" + passwOption.getOpt() + "' option with '-" + tokenOption.getOpt() + "' option");

      if (user == null)
        throw new MissingArgumentException(usernameOption);

      if (loginOptions != null && cl.hasOption(tokenOption.getOpt())) {
        Properties props = new Properties();
        for (String loginOption : loginOptions)
          for (String lo : loginOption.split(",")) {
            String[] split = lo.split("=");
            props.put(split[0], split[1]);
          }
        
        this.token = Class.forName(cl.getOptionValue(tokenOption.getOpt())).asSubclass(AuthenticationToken.class).newInstance();
        this.token.init(props);
      }

      if (!cl.hasOption(fakeOption.getLongOpt())) {
        DistributedTrace.enable(instance, new ZooReader(instance.getZooKeepers(), instance.getZooKeepersSessionTimeOut()), "shell", InetAddress.getLocalHost()
            .getHostName());
      }
      
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void start() {
          reader.getTerminal().enableEcho();
        }
      });
      
      if (passw != null) {
        this.token = new PasswordToken(passw);
      }
      
      if (this.token == null) {
        passw = readMaskedLine("Password: ", '*');
        if (passw != null)
          this.token = new PasswordToken(passw);
      }
      
      if (this.token == null) {
        reader.printNewline();
        throw new MissingArgumentException("No password or token option supplied");
      } // user canceled
      
      this.setTableName("");
      this.principal = user;
      connector = instance.getConnector(this.principal, token);
      
    } catch (Exception e) {
      printException(e);
      configError = true;
    }
    
    // decide whether to execute commands from a file and quit
    if (cl.hasOption(execfileOption.getOpt())) {
      execFile = cl.getOptionValue(execfileOption.getOpt());
      verbose = false;
    } else if (cl.hasOption(execfileVerboseOption.getOpt())) {
      execFile = cl.getOptionValue(execfileVerboseOption.getOpt());
    }
    if (cl.hasOption(execCommandOpt.getOpt())) {
      execCommand = cl.getOptionValue(execCommandOpt.getOpt());
      verbose = false;
    }
    
    rootToken = new Token();
    
    Command[] dataCommands = {new DeleteCommand(), new DeleteManyCommand(), new DeleteRowsCommand(), new EGrepCommand(), new FormatterCommand(),
        new InterpreterCommand(), new GrepCommand(), new ImportDirectoryCommand(), new InsertCommand(), new MaxRowCommand(), new ScanCommand()};
    Command[] debuggingCommands = {new ClasspathCommand(), new DebugCommand(), new ListScansCommand(), new ListCompactionsCommand(), new TraceCommand(),
        new PingCommand()};
    Command[] execCommands = {new ExecfileCommand(), new HistoryCommand()};
    Command[] exitCommands = {new ByeCommand(), new ExitCommand(), new QuitCommand()};
    Command[] helpCommands = {new AboutCommand(), new HelpCommand(), new InfoCommand(), new QuestionCommand()};
    Command[] iteratorCommands = {new DeleteIterCommand(), new DeleteScanIterCommand(), new ListIterCommand(), new SetIterCommand(), new SetScanIterCommand(),
        new SetShellIterCommand(), new ListShellIterCommand(), new DeleteShellIterCommand()};
    Command[] otherCommands = {new HiddenCommand()};
    Command[] permissionsCommands = {new GrantCommand(), new RevokeCommand(), new SystemPermissionsCommand(), new TablePermissionsCommand(),
        new UserPermissionsCommand()};
    Command[] stateCommands = {new AuthenticateCommand(), new ClsCommand(), new ClearCommand(), new NoTableCommand(), new SleepCommand(), new TableCommand(),
        new UserCommand(), new WhoAmICommand()};
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
