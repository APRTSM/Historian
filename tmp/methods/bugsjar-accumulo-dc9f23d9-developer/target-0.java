  public boolean validateOptions(Map<String,String> options) {
    super.validateOptions(options);
    boolean hasStart = false;
    boolean hasEnd = false;
    try {
      if (options.containsKey(START)) {
        hasStart = true;
        String s = options.get(START);
        if (s.startsWith(LONG_PREFIX))
          Long.valueOf(s.substring(LONG_PREFIX.length()));
        else
          dateParser.parse(s);
      }
      if (options.containsKey(END)) {
        hasEnd = true;
        String s = options.get(END);
        if (s.startsWith(LONG_PREFIX))
          Long.valueOf(s.substring(LONG_PREFIX.length()));
        else
          dateParser.parse(s);
      }
      if (!hasStart && !hasEnd)
        return false;
      if (options.get(START_INCL) != null)
        Boolean.parseBoolean(options.get(START_INCL));
      if (options.get(END_INCL) != null)
        Boolean.parseBoolean(options.get(END_INCL));
    } catch (Exception e) {
      return false;
    }
    return true;
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
      if (hasStart) {
        String s = options.get(START);
        if (s.startsWith(LONG_PREFIX))
          start = Long.valueOf(s.substring(LONG_PREFIX.length()));
        else
          start = dateParser.parse(s).getTime();
      }
      if (hasEnd) {
        String s = options.get(END);
        if (s.startsWith(LONG_PREFIX))
          end = Long.valueOf(s.substring(LONG_PREFIX.length()));
        else
          end = dateParser.parse(s).getTime();
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
    if (options.get(START_INCL) != null)
      startInclusive = Boolean.parseBoolean(options.get(START_INCL));
    if (options.get(END_INCL) != null)
      endInclusive = Boolean.parseBoolean(options.get(END_INCL));
  }
  public static void setEnd(IteratorSetting is, String end, boolean endInclusive) {
    SimpleDateFormat dateParser = initDateParser();
    try {
      long endTS = dateParser.parse(end).getTime();
      setEnd(is, endTS, endInclusive);
    } catch (ParseException e) {
      throw new IllegalArgumentException("couldn't parse " + end);
    }
  }
  public static void setStart(IteratorSetting is, String start, boolean startInclusive) {
    SimpleDateFormat dateParser = initDateParser();
    try {
      long startTS = dateParser.parse(start).getTime();
      setStart(is, startTS, startInclusive);
    } catch (ParseException e) {
      throw new IllegalArgumentException("couldn't parse " + start);
    }
  }
  public IteratorOptions describeOptions() {
    IteratorOptions io = super.describeOptions();
    io.setName("tsfilter");
    io.setDescription("TimestampFilter displays entries with timestamps between specified values");
    io.addNamedOption("start", "start timestamp (yyyyMMddHHmmssz or LONG<longstring>)");
    io.addNamedOption("end", "end timestamp (yyyyMMddHHmmssz or LONG<longstring>)");
    io.addNamedOption("startInclusive", "true or false");
    io.addNamedOption("endInclusive", "true or false");
    return io;
  }
  public static void setStart(IteratorSetting is, long start, boolean startInclusive) {
    is.addOption(START, LONG_PREFIX + Long.toString(start));
    is.addOption(START_INCL, Boolean.toString(startInclusive));
  }
  public static void setEnd(IteratorSetting is, long end, boolean endInclusive) {
    is.addOption(END, LONG_PREFIX + Long.toString(end));
    is.addOption(END_INCL, Boolean.toString(endInclusive));
  }
