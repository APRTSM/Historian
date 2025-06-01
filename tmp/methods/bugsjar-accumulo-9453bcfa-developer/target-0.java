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
