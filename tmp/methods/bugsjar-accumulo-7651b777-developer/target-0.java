  public boolean config(String... args) {
    // If configuring the shell failed, fail quickly
    if (!super.config(args)) {
      return false;
    }

    // Update the ConsoleReader with the input and output "redirected"
    try {
      this.reader = new ConsoleReader(in, writer);
    } catch (Exception e) {
      printException(e);
      return false;
    }

    // Don't need this for testing purposes
    this.reader.setUseHistory(false);
    this.reader.setUsePagination(false);

    // Make the parsing from the client easier;
    this.verbose = false;
    return true;
  }
  public int start() throws IOException {
    String input;
    if (isVerbose())
      printInfo();

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

      reader.setDefaultPrompt(getDefaultPrompt());
      input = reader.readLine();
      if (input == null) {
        reader.printNewline();
        return exitCode;
      } // user canceled

      execCommand(input, false, false);
    }
  }
