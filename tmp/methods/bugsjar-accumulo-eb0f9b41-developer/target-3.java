  public Options getOptions() {
    final Options o = new Options();
    forceOpt = new Option("f", "force", false, "delete data even if start or end are not specified");
    o.addOption(OptUtil.startRowOpt());
    o.addOption(OptUtil.endRowOpt());
    o.addOption(OptUtil.tableOpt("table to delete a row range from"));
    o.addOption(forceOpt);
    return o;
  }
  public Options getOptions() {
    final Options o = new Options();
    verboseOpt = new Option("v", "verbose", false, "verbose output during merge");
    sizeOpt = new Option("s", "size", true, "merge tablets to the given size over the entire table");
    forceOpt = new Option("f", "force", false, "merge small tablets to large tablets, even if it goes over the given size");
    allOpt = new Option("", "all", false, "allow an entire table to be merged into one tablet without prompting the user for confirmation");
    o.addOption(OptUtil.startRowOpt());
    o.addOption(OptUtil.endRowOpt());
    o.addOption(OptUtil.tableOpt("table to be merged"));
    o.addOption(verboseOpt);
    o.addOption(sizeOpt);
    o.addOption(forceOpt);
    o.addOption(allOpt);
    return o;
  }
  public static Option startRowOpt() {
    final Option o = new Option(START_ROW_OPT, "begin-row", true, "begin row (exclusive)");
    o.setArgName("begin-row");
    return o;
  }
