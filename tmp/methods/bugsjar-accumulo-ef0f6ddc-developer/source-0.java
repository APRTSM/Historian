  public int start() throws IOException {
    if (configError)
      return 1;
    
    String input;
    if (isVerbose())
      printInfo();
    
    if (execFile != null) {
      java.util.Scanner scanner = new java.util.Scanner(new File(execFile));
      while (scanner.hasNextLine())
        execCommand(scanner.nextLine(), true, isVerbose());
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
