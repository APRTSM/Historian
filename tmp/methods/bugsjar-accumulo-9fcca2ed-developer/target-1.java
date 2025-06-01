  public void createTable(String username, String tableName, boolean useVersions, TimeType timeType) {
    MockTable t = new MockTable(useVersions, timeType, Integer.toString(tableIdCounter.incrementAndGet()));
    t.userPermissions.put(username, EnumSet.allOf(TablePermission.class));
    tables.put(tableName, t);
  }
  MockTable(boolean limitVersion, TimeType timeType, String tableId) {
    this.timeType = timeType;
    this.tableId = tableId;
    settings = IteratorUtil.generateInitialTableProperties(limitVersion);
    for (Entry<String,String> entry : AccumuloConfiguration.getDefaultConfiguration()) {
      String key = entry.getKey();
      if (key.startsWith(Property.TABLE_PREFIX.getKey()))
        settings.put(key, entry.getValue());
    }
  }
  public String getTableId() {
    return this.tableId;
  }
