  public int start() throws IOException {
    if (configError)
      return 1;
    
    String input;
    if (isVerbose())
      printInfo();
    
    String home = System.getProperty("HOME");
    if (home == null)
      home = System.getenv("HOME");
    String configDir = home + "/" + HISTORY_DIR_NAME;
    String historyPath = configDir + "/" + HISTORY_FILE_NAME;
    File accumuloDir = new File(configDir);
    if (!accumuloDir.exists() && !accumuloDir.mkdirs())
      log.warn("Unable to make directory for history at " + accumuloDir);
    try {
      final FileHistory history = new FileHistory(new File(historyPath));
      reader.setHistory(history);
      // Add shutdown hook to flush file history, per jline javadocs
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          try {
            history.flush();
          } catch (IOException e) {
            log.warn("Could not flush history to file.");
          }
        }
      });
    } catch (IOException e) {
      log.warn("Unable to load history file at " + historyPath);
    }
    
    // This would be a nice feature but !METADATA screws it up
    reader.setExpandEvents(false);
    
    ShellCompletor userCompletor = null;
    
    if (false) {
      if (execFile != null) {
        java.util.Scanner scanner = new java.util.Scanner(execFile);
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
    }
    
    while (true) {
      if (hasExited())
        return exitCode;
      
      // If tab completion is true we need to reset
      if (tabCompletion) {
        if (userCompletor != null)
          reader.removeCompleter(userCompletor);
        
        userCompletor = setupCompletion();
        reader.addCompleter(userCompletor);
      }
      
      reader.setPrompt(getDefaultPrompt());
      input = reader.readLine();
      if (input == null) {
        reader.println();
        return exitCode;
      } // user canceled
      
      execCommand(input, disableAuthTimeout, false);
    }
  }
