  public static Option startRowOpt() {
    final Option o = new Option(START_ROW_OPT, "begin-row", true, "begin row (exclusive)");
    o.setArgName("begin-row");
    return o;
  }
