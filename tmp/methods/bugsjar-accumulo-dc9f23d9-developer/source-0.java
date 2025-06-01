  public static void setEnd(IteratorSetting is, long end, boolean endInclusive) {
    SimpleDateFormat dateParser = initDateParser();
    is.addOption(END, dateParser.format(new Date(end)));
    is.addOption(END_INCL, Boolean.toString(endInclusive));
  }
  public IteratorOptions describeOptions() {
    IteratorOptions io = super.describeOptions();
    io.setName("tsfilter");
    io.setDescription("TimestampFilter displays entries with timestamps between specified values");
    io.addNamedOption("start", "start timestamp (yyyyMMddHHmmssz)");
    io.addNamedOption("end", "end timestamp (yyyyMMddHHmmssz)");
    io.addNamedOption("startInclusive", "true or false");
    io.addNamedOption("endInclusive", "true or false");
    return io;
  }
  public static void setEnd(IteratorSetting is, String end, boolean endInclusive) {
    is.addOption(END, end);
    is.addOption(END_INCL, Boolean.toString(endInclusive));
  }
  public static void setStart(IteratorSetting is, long start, boolean startInclusive) {
    SimpleDateFormat dateParser = initDateParser();
    is.addOption(START, dateParser.format(new Date(start)));
    is.addOption(START_INCL, Boolean.toString(startInclusive));
  }
  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws IOException {
    super.init(source, options, env);
    
    if (options == null)
      throw new IllegalArgumentException("start and/or end must be set for " + TimestampFilter.class.getName());
    
    hasStart = false;
    hasEnd = false;
    startInclusive = true;
    endInclusive = true;
    
    if (options.containsKey(START))
      hasStart = true;
    if (options.containsKey(END))
      hasEnd = true;
    if (!hasStart && !hasEnd)
      throw new IllegalArgumentException("must have either start or end for " + TimestampFilter.class.getName());
    
    try {
      if (hasStart)
        start = dateParser.parse(options.get(START)).getTime();
      if (hasEnd)
        end = dateParser.parse(options.get(END)).getTime();
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
    if (options.get(START_INCL) != null)
      startInclusive = Boolean.parseBoolean(options.get(START_INCL));
    if (options.get(END_INCL) != null)
      endInclusive = Boolean.parseBoolean(options.get(END_INCL));
  }
  public boolean validateOptions(Map<String,String> options) {
    super.validateOptions(options);
    try {
      if (options.containsKey(START))
        dateParser.parse(options.get(START));
      if (options.containsKey(END))
        dateParser.parse(options.get(END));
      if (options.get(START_INCL) != null)
        Boolean.parseBoolean(options.get(START_INCL));
      if (options.get(END_INCL) != null)
        Boolean.parseBoolean(options.get(END_INCL));
    } catch (Exception e) {
      return false;
    }
    return true;
  }
  public static void setStart(IteratorSetting is, String start, boolean startInclusive) {
    is.addOption(START, start);
    is.addOption(START_INCL, Boolean.toString(startInclusive));
  }
