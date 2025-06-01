  public void create(String tableName, boolean limitVersion, TimeType timeType) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    ArgumentChecker.notNull(tableName, timeType);
    
    List<ByteBuffer> args = Arrays.asList(ByteBuffer.wrap(tableName.getBytes()), ByteBuffer.wrap(timeType.name().getBytes()));
    
    Map<String,String> opts = IteratorUtil.generateInitialTableProperties(limitVersion);
    
    try {
      doTableOperation(TableOperation.CREATE, args, opts);
    } catch (TableNotFoundException e1) {
      // should not happen
      throw new RuntimeException(e1);
    }
  }
  MockTable(boolean limitVersion, TimeType timeType) {
    this.timeType = timeType;
    settings = IteratorUtil.generateInitialTableProperties(limitVersion);
    for (Entry<String,String> entry : AccumuloConfiguration.getDefaultConfiguration()) {
      String key = entry.getKey();
      if (key.startsWith(Property.TABLE_PREFIX.getKey()))
        settings.put(key, entry.getValue());
    }
  }
  public static Map<String,String> generateInitialTableProperties(boolean limitVersion) {
    TreeMap<String,String> props = new TreeMap<String,String>();
    
    if (limitVersion) {
        for (IteratorScope iterScope : IteratorScope.values()) {
          props.put(Property.TABLE_ITERATOR_PREFIX + iterScope.name() + ".vers", "20," + VersioningIterator.class.getName());
          props.put(Property.TABLE_ITERATOR_PREFIX + iterScope.name() + ".vers.opt.maxVersions", "1");
        }
    }

    return props;
  }
  public int execute(final String fullCommand, final CommandLine cl, final Shell shellState) throws AccumuloException, AccumuloSecurityException, TableExistsException,
      TableNotFoundException, IOException, ClassNotFoundException {
    
    final String testTableName = cl.getArgs()[0];
    
    if (!testTableName.matches(Constants.VALID_TABLE_NAME_REGEX)) {
      shellState.getReader().printString("Only letters, numbers and underscores are allowed for use in table names. \n");
      throw new IllegalArgumentException();
    }
    
    final String tableName = cl.getArgs()[0];
    if (shellState.getConnector().tableOperations().exists(tableName)) {
      throw new TableExistsException(null, tableName, null);
    }
    final SortedSet<Text> partitions = new TreeSet<Text>();
    final boolean decode = cl.hasOption(base64Opt.getOpt());
    
    if (cl.hasOption(createTableOptSplit.getOpt())) {
      final String f = cl.getOptionValue(createTableOptSplit.getOpt());
      
      String line;
      Scanner file = new Scanner(new File(f));
      while (file.hasNextLine()) {
        line = file.nextLine();
        if (!line.isEmpty()) {
          partitions.add(decode ? new Text(Base64.decodeBase64(line.getBytes())) : new Text(line));
        }
      }
    } else if (cl.hasOption(createTableOptCopySplits.getOpt())) {
      final String oldTable = cl.getOptionValue(createTableOptCopySplits.getOpt());
      if (!shellState.getConnector().tableOperations().exists(oldTable)) {
        throw new TableNotFoundException(null, oldTable, null);
      }
      partitions.addAll(shellState.getConnector().tableOperations().getSplits(oldTable));
    }
    
    if (cl.hasOption(createTableOptCopyConfig.getOpt())) {
      final String oldTable = cl.getOptionValue(createTableOptCopyConfig.getOpt());
      if (!shellState.getConnector().tableOperations().exists(oldTable)) {
        throw new TableNotFoundException(null, oldTable, null);
      }
    }
    
    TimeType timeType = TimeType.MILLIS;
    if (cl.hasOption(createTableOptTimeLogical.getOpt())) {
      timeType = TimeType.LOGICAL;
    }
    
    // create table
    shellState.getConnector().tableOperations().create(tableName, true, timeType);
    if (partitions.size() > 0) {
      shellState.getConnector().tableOperations().addSplits(tableName, partitions);
    }
    shellState.setTableName(tableName); // switch shell to new table
    // context
    
    if (cl.hasOption(createTableNoDefaultIters.getOpt())) {
      for (String key : IteratorUtil.generateInitialTableProperties(true).keySet()) {
        shellState.getConnector().tableOperations().removeProperty(tableName, key);
      }
    }
    
    // Copy options if flag was set
    if (cl.hasOption(createTableOptCopyConfig.getOpt())) {
      if (shellState.getConnector().tableOperations().exists(tableName)) {
        final Iterable<Entry<String,String>> configuration = shellState.getConnector().tableOperations()
            .getProperties(cl.getOptionValue(createTableOptCopyConfig.getOpt()));
        for (Entry<String,String> entry : configuration) {
          if (Property.isValidTablePropertyKey(entry.getKey())) {
            shellState.getConnector().tableOperations().setProperty(tableName, entry.getKey(), entry.getValue());
          }
        }
      }
    }
    
    if (cl.hasOption(createTableOptEVC.getOpt())) {
      try {
        shellState.getConnector().tableOperations().addConstraint(tableName, VisibilityConstraint.class.getName());
      } catch (AccumuloException e) {
        Shell.log.warn(e.getMessage() + " while setting visibility constraint, but table was created");
      }
    }
    
    // Load custom formatter if set
    if (cl.hasOption(createTableOptFormatter.getOpt())) {
      final String formatterClass = cl.getOptionValue(createTableOptFormatter.getOpt());
      
      shellState.getConnector().tableOperations().setProperty(tableName, Property.TABLE_FORMATTER_CLASS.toString(), formatterClass);
    }
    
    return 0;
  }
