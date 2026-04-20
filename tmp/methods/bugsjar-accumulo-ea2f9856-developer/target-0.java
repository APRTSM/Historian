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
      case 8:
        return "Bulk load transaction no longer running";
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
        violations = addIfNotPresent(violations, 4);
      }
    }
    
    if (!containsSemiC) {
      // see if last row char is <
      if (row.length == 0 || row[row.length - 1] != '<') {
        violations = addIfNotPresent(violations, 4);
      }
    } else {
      if (row.length == 0) {
        violations = addIfNotPresent(violations, 4);
      }
    }
    
    if (row.length > 0 && row[0] == '!') {
      if (row.length < 3 || row[1] != '0' || (row[2] != '<' && row[2] != ';')) {
        violations = addIfNotPresent(violations, 4);
      }
    }
    
    // ensure row is not less than Constants.METADATA_TABLE_ID
    if (new Text(row).compareTo(new Text(Constants.METADATA_TABLE_ID)) < 0) {
      violations = addViolation(violations, 5);
    }
    
    for (ColumnUpdate columnUpdate : colUpdates) {
      Text columnFamily = new Text(columnUpdate.getColumnFamily());
      
      if (columnUpdate.isDeleted()) {
        if (!isValidColumn(columnUpdate)) {
          violations = addViolation(violations, 2);
        }
        continue;
      }
      
      if (columnUpdate.getValue().length == 0 && !columnFamily.equals(Constants.METADATA_SCANFILE_COLUMN_FAMILY)) {
        violations = addViolation(violations, 6);
      }
      
      if (columnFamily.equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY)) {
        try {
          DataFileValue dfv = new DataFileValue(columnUpdate.getValue());
          
          if (dfv.getSize() < 0 || dfv.getNumEntries() < 0) {
            violations = addViolation(violations, 1);
          }
        } catch (NumberFormatException nfe) {
          violations = addViolation(violations, 1);
        } catch (ArrayIndexOutOfBoundsException aiooe) {
          violations = addViolation(violations, 1);
        }
      } else if (columnFamily.equals(Constants.METADATA_SCANFILE_COLUMN_FAMILY)) {
        
      } else if (columnFamily.equals(Constants.METADATA_BULKFILE_COLUMN_FAMILY)) {
        if (!columnUpdate.isDeleted()) {
          // splits, which also write the time reference, are allowed to write this reference even when
          // the transaction is not running because the other half of the tablet is holding a reference
          // to the file.
          boolean isSplitMutation = false;
          // When a tablet is assigned, it re-writes the metadata.  It should probably only update the location information,
          // but it writes everything.  We allow it to re-write the bulk information if it is setting the location.
          // See ACCUMULO-1230.
          boolean isLocationMutation = false;
          for (ColumnUpdate update : mutation.getUpdates()) {
            if (new ColumnFQ(update).equals(Constants.METADATA_TIME_COLUMN)) {
              isSplitMutation = true;
            }
            if (update.getColumnFamily().equals(Constants.METADATA_CURRENT_LOCATION_COLUMN_FAMILY)) {
              isLocationMutation = true;
            }
          }

          if (!isSplitMutation && !isLocationMutation) {
            String tidString = new String(columnUpdate.getValue());
            long tid = Long.parseLong(tidString);
            try {
              if (!new ZooArbitrator().transactionAlive(Constants.BULK_ARBITRATOR_TYPE, tid)) {
                violations = addViolation(violations, 8);
              }
            } catch (Exception ex) {
              violations = addViolation(violations, 8);
            }
          }
        }
      } else {
        if (!isValidColumn(columnUpdate)) {
          violations = addViolation(violations, 2);
        } else if (new ColumnFQ(columnUpdate).equals(Constants.METADATA_PREV_ROW_COLUMN) && columnUpdate.getValue().length > 0
            && (violations == null || !violations.contains((short) 4))) {
          KeyExtent ke = new KeyExtent(new Text(mutation.getRow()), (Text) null);
          
          Text per = KeyExtent.decodePrevEndRow(new Value(columnUpdate.getValue()));
          
          boolean prevEndRowLessThanEndRow = per == null || ke.getEndRow() == null || per.compareTo(ke.getEndRow()) < 0;
          
          if (!prevEndRowLessThanEndRow) {
            violations = addViolation(violations, 3);
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
            violations = addViolation(violations, 7);
          }
        }
        
      }
    }
    
    if (violations != null) {
      log.debug("violating metadata mutation : " + new String(mutation.getRow()));
      for (ColumnUpdate update : mutation.getUpdates()) {
        log.debug(" update: " + new String(update.getColumnFamily()) + ":" + new String(update.getColumnQualifier()) + " value " + (update.isDeleted() ? "[delete]" : new String(update.getValue())));
      }
    }
    
    return violations;
  }
  static private ArrayList<Short> addViolation(ArrayList<Short> lst, int violation) {
    if (lst == null)
      lst = new ArrayList<Short>();
    lst.add((short)violation);
    return lst;
  }
  static private ArrayList<Short> addIfNotPresent(ArrayList<Short> lst, int intViolation) {
    if (lst == null)
      return addViolation(lst, intViolation);
    short violation = (short)intViolation;
    if (!lst.contains(violation))
      return addViolation(lst, intViolation);
    return lst;
  }
