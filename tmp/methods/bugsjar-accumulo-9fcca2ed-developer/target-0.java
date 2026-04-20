  public void createTable(String username, String tableName, boolean useVersions, TimeType timeType) {
    MockTable t = new MockTable(useVersions, timeType, Integer.toString(tableIdCounter.incrementAndGet()));
    t.userPermissions.put(username, EnumSet.allOf(TablePermission.class));
    tables.put(tableName, t);
  }
