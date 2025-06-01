  public boolean validateOptions(Map<String,String> options) {
    super.validateOptions(options);
    if (options.containsKey(ROW_REGEX))
      Pattern.compile(options.get(ROW_REGEX)).matcher("");
    
    if (options.containsKey(COLF_REGEX))
      Pattern.compile(options.get(COLF_REGEX)).matcher("");
    
    if (options.containsKey(COLQ_REGEX))
      Pattern.compile(options.get(COLQ_REGEX)).matcher("");
    
    if (options.containsKey(VALUE_REGEX))
      Pattern.compile(options.get(VALUE_REGEX)).matcher("");
    
    return true;
  }
  public IteratorOptions describeOptions() {
    IteratorOptions io = super.describeOptions();
    io.setName("regex");
    io.setDescription("The RegExFilter/Iterator allows you to filter for key/value pairs based on regular expressions");
    io.addNamedOption(RegExFilter.ROW_REGEX, "regular expression on row");
    io.addNamedOption(RegExFilter.COLF_REGEX, "regular expression on column family");
    io.addNamedOption(RegExFilter.COLQ_REGEX, "regular expression on column qualifier");
    io.addNamedOption(RegExFilter.VALUE_REGEX, "regular expression on value");
    io.addNamedOption(RegExFilter.OR_FIELDS, "use OR instread of AND when multiple regexes given");
    return io;
  }
  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws IOException {
    super.init(source, options, env);
    if (options.containsKey(ROW_REGEX)) {
      rowMatcher = Pattern.compile(options.get(ROW_REGEX)).matcher("");
    } else {
      rowMatcher = null;
    }
    
    if (options.containsKey(COLF_REGEX)) {
      colfMatcher = Pattern.compile(options.get(COLF_REGEX)).matcher("");
    } else {
      colfMatcher = null;
    }
    
    if (options.containsKey(COLQ_REGEX)) {
      colqMatcher = Pattern.compile(options.get(COLQ_REGEX)).matcher("");
    } else {
      colqMatcher = null;
    }
    
    if (options.containsKey(VALUE_REGEX)) {
      valueMatcher = Pattern.compile(options.get(VALUE_REGEX)).matcher("");
    } else {
      valueMatcher = null;
    }
    
    if (options.containsKey(OR_FIELDS)) {
      orFields = Boolean.parseBoolean(options.get(OR_FIELDS));
    } else {
      orFields = false;
    }
  }
  private Matcher copyMatcher(Matcher m)
  {
	  if(m == null)
		  return m;
	  else
		  return m.pattern().matcher("");
  }
  public static void setRegexs(IteratorSetting si, String rowTerm, String cfTerm, String cqTerm, String valueTerm, boolean orFields) {
    if (rowTerm != null)
      si.addOption(RegExFilter.ROW_REGEX, rowTerm);
    if (cfTerm != null)
      si.addOption(RegExFilter.COLF_REGEX, cfTerm);
    if (cqTerm != null)
      si.addOption(RegExFilter.COLQ_REGEX, cqTerm);
    if (valueTerm != null)
      si.addOption(RegExFilter.VALUE_REGEX, valueTerm);
    if (orFields) {
      si.addOption(RegExFilter.OR_FIELDS, "true");
    }
  }
  private boolean matches(Matcher matcher, ByteSequence bs) {
    if (matcher != null) {
      babcs.set(bs);
      matcher.reset(babcs);
      return matcher.matches();
    }
    
    return !orFields;
  }
  private boolean matches(Matcher matcher, byte data[], int offset, int len) {
    if (matcher != null) {
      babcs.set(data, offset, len);
      matcher.reset(babcs);
      return matcher.matches();
    }
    
    return !orFields;
  }
