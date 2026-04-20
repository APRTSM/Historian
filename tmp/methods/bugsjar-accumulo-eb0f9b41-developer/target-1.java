  public Options getOptions() {
    final Options o = new Options();
    forceOpt = new Option("f", "force", false, "delete data even if start or end are not specified");
    o.addOption(OptUtil.startRowOpt());
    o.addOption(OptUtil.endRowOpt());
    o.addOption(OptUtil.tableOpt("table to delete a row range from"));
    o.addOption(forceOpt);
    return o;
  }
