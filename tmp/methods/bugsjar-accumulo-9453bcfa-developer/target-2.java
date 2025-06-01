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
