  public int start() throws IOException {
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
      java.util.Scanner scanner = new java.util.Scanner(new File(execFile), UTF_8.name());
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
