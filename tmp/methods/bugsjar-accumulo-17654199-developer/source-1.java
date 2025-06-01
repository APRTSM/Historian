  protected static void initMetadataConfig() throws IOException {
    initMetadataConfig(RootTable.ID);
    initMetadataConfig(MetadataTable.ID);
  }
  public static void updateFiles(Credentials creds, KeyExtent extent, Collection<String> files, Status stat) {
    if (log.isDebugEnabled()) {
      log.debug("Updating replication for " + extent + " with " + files + " using " + ProtobufUtil.toString(stat));
    }
    // TODO could use batch writer, would need to handle failure and retry like update does - ACCUMULO-1294
    if (files.isEmpty()) {
      return;
    }

    Value v = ProtobufUtil.toValue(stat);
    for (String file : files) {
      // TODO Can preclude this addition if the extent is for a table we don't need to replicate
      update(creds, createUpdateMutation(new Path(file), v, extent), extent);
    }
  }
