  public void printVerboseInfo() throws IOException {
    StringBuilder sb = new StringBuilder("-\n");
    sb.append("- Current user: ").append(connector.whoami()).append("\n");
    if (execFile != null)
      sb.append("- Executing commands from: ").append(execFile).append("\n");
    if (disableAuthTimeout)
      sb.append("- Authorization timeout: disabled\n");
    else
      sb.append("- Authorization timeout: ").append(String.format("%ds%n", TimeUnit.NANOSECONDS.toSeconds(authTimeout)));
    sb.append("- Debug: ").append(isDebuggingEnabled() ? "on" : "off").append("\n");
    if (!scanIteratorOptions.isEmpty()) {
      for (Entry<String,List<IteratorSetting>> entry : scanIteratorOptions.entrySet()) {
        sb.append("- Session scan iterators for table ").append(entry.getKey()).append(":\n");
        for (IteratorSetting setting : entry.getValue()) {
          sb.append("-    Iterator ").append(setting.getName()).append(" options:\n");
          sb.append("-        ").append("iteratorPriority").append(" = ").append(setting.getPriority()).append("\n");
          sb.append("-        ").append("iteratorClassName").append(" = ").append(setting.getIteratorClass()).append("\n");
          for (Entry<String,String> optEntry : setting.getOptions().entrySet()) {
            sb.append("-        ").append(optEntry.getKey()).append(" = ").append(optEntry.getValue()).append("\n");
          }
        }
      }
    }
    sb.append("-\n");
    reader.printString(sb.toString());
  }
