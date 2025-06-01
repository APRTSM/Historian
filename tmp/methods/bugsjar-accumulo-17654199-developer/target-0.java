  protected static void initMetadataConfig() throws IOException {
    initMetadataConfig(RootTable.ID);
    initMetadataConfig(MetadataTable.ID);

    // ACCUMULO-3077 Set the combiner on accumulo.metadata during init to reduce the likelihood of a race
    // condition where a tserver compacts away Status updates because it didn't see the Combiner configured
    IteratorSetting setting = new IteratorSetting(9, ReplicationTableUtil.COMBINER_NAME, StatusCombiner.class);
    Combiner.setColumns(setting, Collections.singletonList(new Column(MetadataSchema.ReplicationSection.COLF)));
    try {
      for (IteratorScope scope : IteratorScope.values()) {
        String root = String.format("%s%s.%s", Property.TABLE_ITERATOR_PREFIX, scope.name().toLowerCase(), setting.getName());
        for (Entry<String,String> prop : setting.getOptions().entrySet()) {
          TablePropUtil.setTableProperty(MetadataTable.ID, root + ".opt." + prop.getKey(), prop.getValue());
        }
        TablePropUtil.setTableProperty(MetadataTable.ID, root, setting.getPriority() + "," + setting.getIteratorClass());
      }
    } catch (Exception e) {
      log.fatal("Error talking to ZooKeeper", e);
      throw new IOException(e);
    }
  }
