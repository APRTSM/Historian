  public void createTable(String username, String tableName, boolean useVersions, TimeType timeType) {
    MockTable t = new MockTable(useVersions, timeType);
    t.userPermissions.put(username, EnumSet.allOf(TablePermission.class));
    tables.put(tableName, t);
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
  public void merge(Text start, Text end) {
    boolean reAdd = false;
    if (splits.contains(start))
      reAdd = true;
    splits.removeAll(splits.subSet(start, end));
    if (reAdd)
      splits.add(start);
  }
