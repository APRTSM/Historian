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
    final Option o = new Option(START_ROW_OPT, "begin-row", true, "begin row (NOT) inclusive");
    o.setArgName("begin-row");
    return o;
  }
  public Options getOptions() {
    final Options o = new Options();

    scanOptAuths = new Option("s", "scan-authorizations", true, "scan authorizations (all user auths are used if this argument is not specified)");
    optStartRowExclusive = new Option("be", "begin-exclusive", false, "make start row exclusive (by default it's inclusive)");
    optStartRowExclusive.setArgName("begin-exclusive");
    optEndRowExclusive = new Option("ee", "end-exclusive", false, "make end row exclusive (by default it's inclusive)");
    optEndRowExclusive.setArgName("end-exclusive");
    scanOptRow = new Option("r", "row", true, "row to scan");
    scanOptColumns = new Option("c", "columns", true, "comma-separated columns");
    timestampOpt = new Option("st", "show-timestamps", false, "display timestamps");
    disablePaginationOpt = new Option("np", "no-pagination", false, "disable pagination of output");
    showFewOpt = new Option("f", "show-few", true, "show only a specified number of characters");
    formatterOpt = new Option("fm", "formatter", true, "fully qualified name of the formatter class to use");
    interpreterOpt = new Option("i", "interpreter", true, "fully qualified name of the interpreter class to use");
    formatterInterpeterOpt = new Option("fi", "fmt-interpreter", true, "fully qualified name of a class that is a formatter and interpreter");
    timeoutOption = new Option(null, "timeout", true,
        "time before scan should fail if no data is returned. If no unit is given assumes seconds.  Units d,h,m,s,and ms are supported.  e.g. 30s or 100ms");
    outputFileOpt = new Option("o", "output", true, "local file to write the scan output to");

    scanOptAuths.setArgName("comma-separated-authorizations");
    scanOptRow.setArgName("row");
    scanOptColumns.setArgName("<columnfamily>[:<columnqualifier>]{,<columnfamily>[:<columnqualifier>]}");
    showFewOpt.setRequired(false);
    showFewOpt.setArgName("int");
    formatterOpt.setArgName("className");
    timeoutOption.setArgName("timeout");
    outputFileOpt.setArgName("file");

    profileOpt = new Option("pn", "profile", true, "iterator profile name");
    profileOpt.setArgName("profile");

    o.addOption(scanOptAuths);
    o.addOption(scanOptRow);
    optStartRowInclusive = new Option(OptUtil.START_ROW_OPT, "begin-row", true, "begin row (inclusive)");
    optStartRowInclusive.setArgName("begin-row");
    o.addOption(optStartRowInclusive);
    o.addOption(OptUtil.endRowOpt());
    o.addOption(optStartRowExclusive);
    o.addOption(optEndRowExclusive);
    o.addOption(scanOptColumns);
    o.addOption(timestampOpt);
    o.addOption(disablePaginationOpt);
    o.addOption(OptUtil.tableOpt("table to be scanned"));
    o.addOption(showFewOpt);
    o.addOption(formatterOpt);
    o.addOption(interpreterOpt);
    o.addOption(formatterInterpeterOpt);
    o.addOption(timeoutOption);
    o.addOption(outputFileOpt);
    o.addOption(profileOpt);

    return o;
  }
