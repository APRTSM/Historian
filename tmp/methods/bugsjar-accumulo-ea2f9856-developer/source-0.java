  public String getViolationDescription(short violationCode) {
    switch (violationCode) {
      case 1:
        return "data file size must be a non-negative integer";
      case 2:
        return "Invalid column name given.";
      case 3:
        return "Prev end row is greater than or equal to end row.";
      case 4:
        return "Invalid metadata row format";
      case 5:
        return "Row can not be less than " + Constants.METADATA_TABLE_ID;
      case 6:
        return "Empty values are not allowed for any " + Constants.METADATA_TABLE_NAME + " column";
      case 7:
        return "Lock not held in zookeeper by writer";
    }
    return null;
  }
  public List<Short> check(Environment env, Mutation mutation) {
    
    ArrayList<Short> violations = null;
    
    Collection<ColumnUpdate> colUpdates = mutation.getUpdates();
    
    // check the row, it should contains at least one ; or end with <
    boolean containsSemiC = false;
    
    byte[] row = mutation.getRow();
    
    // always allow rows that fall within reserved area
    if (row.length > 0 && row[0] == '~')
      return null;
    
    for (byte b : row) {
      if (b == ';') {
        containsSemiC = true;
      }
      
      if (b == ';' || b == '<')
        break;
      
      if (!validTableNameChars[0xff & b]) {
        if (violations == null)
          violations = new ArrayList<Short>();
        if (!violations.contains((short) 4))
          violations.add((short) 4);
      }
    }
    
    if (!containsSemiC) {
      // see if last row char is <
      if (row.length == 0 || row[row.length - 1] != '<') {
        if (violations == null)
          violations = new ArrayList<Short>();
        if (!violations.contains((short) 4))
          violations.add((short) 4);
      }
    } else {
      if (row.length == 0) {
        if (violations == null)
          violations = new ArrayList<Short>();
        if (!violations.contains((short) 4))
          violations.add((short) 4);
      }
    }
    
    if (row.length > 0 && row[0] == '!') {
      if (row.length < 3 || row[1] != '0' || (row[2] != '<' && row[2] != ';')) {
        if (violations == null)
          violations = new ArrayList<Short>();
        if (!violations.contains((short) 4))
          violations.add((short) 4);
      }
    }
    
    // ensure row is not less than Constants.METADATA_TABLE_ID
    if (new Text(row).compareTo(new Text(Constants.METADATA_TABLE_ID)) < 0) {
      if (violations == null)
        violations = new ArrayList<Short>();
      violations.add((short) 5);
    }
    
    for (ColumnUpdate columnUpdate : colUpdates) {
      Text columnFamily = new Text(columnUpdate.getColumnFamily());
      
      if (columnUpdate.isDeleted()) {
        if (!isValidColumn(columnUpdate)) {
          if (violations == null)
            violations = new ArrayList<Short>();
          violations.add((short) 2);
        }
        continue;
      }
      
      if (columnUpdate.getValue().length == 0 && !columnFamily.equals(Constants.METADATA_SCANFILE_COLUMN_FAMILY)) {
        if (violations == null)
          violations = new ArrayList<Short>();
        violations.add((short) 6);
      }
      
      if (columnFamily.equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY)) {
        try {
          DataFileValue dfv = new DataFileValue(columnUpdate.getValue());
          
          if (dfv.getSize() < 0 || dfv.getNumEntries() < 0) {
            if (violations == null)
              violations = new ArrayList<Short>();
            violations.add((short) 1);
          }
        } catch (NumberFormatException nfe) {
          if (violations == null)
            violations = new ArrayList<Short>();
          violations.add((short) 1);
        } catch (ArrayIndexOutOfBoundsException aiooe) {
          if (violations == null)
            violations = new ArrayList<Short>();
          violations.add((short) 1);
        }
      } else if (columnFamily.equals(Constants.METADATA_SCANFILE_COLUMN_FAMILY)) {
        
      } else {
        if (!isValidColumn(columnUpdate)) {
          if (violations == null)
            violations = new ArrayList<Short>();
          violations.add((short) 2);
        } else if (new ColumnFQ(columnUpdate).equals(Constants.METADATA_PREV_ROW_COLUMN) && columnUpdate.getValue().length > 0
            && (violations == null || !violations.contains((short) 4))) {
          KeyExtent ke = new KeyExtent(new Text(mutation.getRow()), (Text) null);
          
          Text per = KeyExtent.decodePrevEndRow(new Value(columnUpdate.getValue()));
          
          boolean prevEndRowLessThanEndRow = per == null || ke.getEndRow() == null || per.compareTo(ke.getEndRow()) < 0;
          
          if (!prevEndRowLessThanEndRow) {
            if (violations == null)
              violations = new ArrayList<Short>();
            violations.add((short) 3);
          }
        } else if (new ColumnFQ(columnUpdate).equals(Constants.METADATA_LOCK_COLUMN)) {
          if (zooCache == null) {
            zooCache = new ZooCache();
          }
          
          if (zooRoot == null) {
            zooRoot = ZooUtil.getRoot(HdfsZooInstance.getInstance());
          }
          
          boolean lockHeld = false;
          String lockId = new String(columnUpdate.getValue());
          
          try {
            lockHeld = ZooLock.isLockHeld(zooCache, new ZooUtil.LockID(zooRoot, lockId));
          } catch (Exception e) {
            log.debug("Failed to verify lock was held " + lockId + " " + e.getMessage());
          }
          
          if (!lockHeld) {
            if (violations == null)
              violations = new ArrayList<Short>();
            violations.add((short) 7);
          }
        }
        
      }
    }
    
    if (violations != null) {
      log.debug(" violating metadata mutation : " + mutation);
    }
    
    return violations;
  }
