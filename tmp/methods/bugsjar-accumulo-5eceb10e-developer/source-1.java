  public Path getFullPath(FileType fileType, String path) {
    int colon = path.indexOf(':');
    if (colon > -1) {
      // Check if this is really an absolute path or if this is a 1.4 style relative path for a WAL
      if (fileType == FileType.WAL && path.charAt(colon + 1) != '/') {
        path = path.substring(path.indexOf('/'));
      } else {
        return new Path(path);
      }
    }

    // normalize the path
    Path fullPath = new Path(defaultVolume.getBasePath(), fileType.getDirectory());
    if (path.startsWith("/"))
      path = path.substring(1);
    fullPath = new Path(fullPath, path);

    FileSystem fs = getVolumeByPath(fullPath).getFileSystem();
    return fs.makeQualified(fullPath);
  }
  private void deleteTablets(MergeInfo info) throws AccumuloException {
    KeyExtent extent = info.getExtent();
    String targetSystemTable = extent.isMeta() ? RootTable.NAME : MetadataTable.NAME;
    Master.log.debug("Deleting tablets for " + extent);
    char timeType = '\0';
    KeyExtent followingTablet = null;
    if (extent.getEndRow() != null) {
      Key nextExtent = new Key(extent.getEndRow()).followingKey(PartialKey.ROW);
      followingTablet = getHighTablet(new KeyExtent(extent.getTableId(), nextExtent.getRow(), extent.getEndRow()));
      Master.log.debug("Found following tablet " + followingTablet);
    }
    try {
      Connector conn = this.master.getConnector();
      Text start = extent.getPrevEndRow();
      if (start == null) {
        start = new Text();
      }
      Master.log.debug("Making file deletion entries for " + extent);
      Range deleteRange = new Range(KeyExtent.getMetadataEntry(extent.getTableId(), start), false, KeyExtent.getMetadataEntry(extent.getTableId(),
          extent.getEndRow()), true);
      Scanner scanner = conn.createScanner(targetSystemTable, Authorizations.EMPTY);
      scanner.setRange(deleteRange);
      TabletsSection.ServerColumnFamily.DIRECTORY_COLUMN.fetch(scanner);
      TabletsSection.ServerColumnFamily.TIME_COLUMN.fetch(scanner);
      scanner.fetchColumnFamily(DataFileColumnFamily.NAME);
      scanner.fetchColumnFamily(TabletsSection.CurrentLocationColumnFamily.NAME);
      Set<FileRef> datafiles = new TreeSet<FileRef>();
      for (Entry<Key,Value> entry : scanner) {
        Key key = entry.getKey();
        if (key.compareColumnFamily(DataFileColumnFamily.NAME) == 0) {
          datafiles.add(new FileRef(this.master.fs, key));
          if (datafiles.size() > 1000) {
            MetadataTableUtil.addDeleteEntries(extent, datafiles, SystemCredentials.get());
            datafiles.clear();
          }
        } else if (TabletsSection.ServerColumnFamily.TIME_COLUMN.hasColumns(key)) {
          timeType = entry.getValue().toString().charAt(0);
        } else if (key.compareColumnFamily(TabletsSection.CurrentLocationColumnFamily.NAME) == 0) {
          throw new IllegalStateException("Tablet " + key.getRow() + " is assigned during a merge!");
        } else if (TabletsSection.ServerColumnFamily.DIRECTORY_COLUMN.hasColumns(key)) {
          datafiles.add(new FileRef(entry.getValue().toString(), this.master.fs.getFullPath(FileType.TABLE, entry.getValue().toString())));
          if (datafiles.size() > 1000) {
            MetadataTableUtil.addDeleteEntries(extent, datafiles, SystemCredentials.get());
            datafiles.clear();
          }
        }
      }
      MetadataTableUtil.addDeleteEntries(extent, datafiles, SystemCredentials.get());
      BatchWriter bw = conn.createBatchWriter(targetSystemTable, new BatchWriterConfig());
      try {
        deleteTablets(info, deleteRange, bw, conn);
      } finally {
        bw.close();
      }
      
      if (followingTablet != null) {
        Master.log.debug("Updating prevRow of " + followingTablet + " to " + extent.getPrevEndRow());
        bw = conn.createBatchWriter(targetSystemTable, new BatchWriterConfig());
        try {
          Mutation m = new Mutation(followingTablet.getMetadataEntry());
          TabletsSection.TabletColumnFamily.PREV_ROW_COLUMN.put(m, KeyExtent.encodePrevEndRow(extent.getPrevEndRow()));
          ChoppedColumnFamily.CHOPPED_COLUMN.putDelete(m);
          bw.addMutation(m);
          bw.flush();
        } finally {
          bw.close();
        }
      } else {
        // Recreate the default tablet to hold the end of the table
        Master.log.debug("Recreating the last tablet to point to " + extent.getPrevEndRow());
        String tdir = master.getFileSystem().choose(ServerConstants.getTablesDirs()) + "/" + extent.getTableId() + Constants.DEFAULT_TABLET_LOCATION;
        MetadataTableUtil.addTablet(new KeyExtent(extent.getTableId(), null, extent.getPrevEndRow()), tdir,
            SystemCredentials.get(), timeType, this.master.masterLock);
      }
    } catch (Exception ex) {
      throw new AccumuloException(ex);
    }
  }
