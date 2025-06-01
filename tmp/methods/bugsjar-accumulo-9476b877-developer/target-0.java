  public static void printDiskUsage(AccumuloConfiguration acuConf, Collection<String> tables, FileSystem fs, Connector conn, Printer printer) throws TableNotFoundException,
  IOException {
  
    TableDiskUsage tdu = new TableDiskUsage();
    
    HashSet<String> tableIds = new HashSet<String>();
    
    for (String tableName : tables) {
      String tableId = conn.tableOperations().tableIdMap().get(tableName);
      if (tableId == null)
        throw new TableNotFoundException(null, tableName, "Table " + tableName + " not found");
      
      tableIds.add(tableId);
    }
    
    for (String tableId : tableIds)
      tdu.addTable(tableId);
    
    HashSet<String> tablesReferenced = new HashSet<String>(tableIds);
    HashSet<String> emptyTableIds = new HashSet<String>();
    
    for (String tableId : tableIds) {
      Scanner mdScanner = conn.createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS);
      mdScanner.fetchColumnFamily(Constants.METADATA_DATAFILE_COLUMN_FAMILY);
      mdScanner.setRange(new KeyExtent(new Text(tableId), null, null).toMetadataRange());
      
      if(!mdScanner.iterator().hasNext()) {
        emptyTableIds.add(tableId);
      }

      for (Entry<Key,Value> entry : mdScanner) {
        String file = entry.getKey().getColumnQualifier().toString();
        if (file.startsWith("../")) {
          file = file.substring(2);
          tablesReferenced.add(file.split("\\/")[1]);
        } else
          file = "/" + tableId + file;
        
        tdu.linkFileAndTable(tableId, file);
      }
    }
    
    for (String tableId : tablesReferenced) {
      FileStatus[] files = fs.globStatus(new Path(Constants.getTablesDir(acuConf) + "/" + tableId + "/*/*"));
      
      for (FileStatus fileStatus : files) {
        String dir = fileStatus.getPath().getParent().getName();
        String name = fileStatus.getPath().getName();
        
        tdu.addFileSize("/" + tableId + "/" + dir + "/" + name, fileStatus.getLen());
      }
      
    }
    
    HashMap<String,String> reverseTableIdMap = new HashMap<String,String>();
    for (Entry<String,String> entry : conn.tableOperations().tableIdMap().entrySet())
      reverseTableIdMap.put(entry.getValue(), entry.getKey());
    
    TreeMap<TreeSet<String>,Long> usage = new TreeMap<TreeSet<String>,Long>(new Comparator<TreeSet<String>>() {
      
      @Override
      public int compare(TreeSet<String> o1, TreeSet<String> o2) {
        int len1 = o1.size();
        int len2 = o2.size();
        
        int min = Math.min(len1, len2);
        
        Iterator<String> iter1 = o1.iterator();
        Iterator<String> iter2 = o2.iterator();
        
        int count = 0;
        
        while (count < min) {
          String s1 = iter1.next();
          String s2 = iter2.next();
          
          int cmp = s1.compareTo(s2);
          
          if (cmp != 0)
            return cmp;
          
          count++;
        }
        
        return len1 - len2;
      }
    });
    
    for (Entry<List<String>,Long> entry : tdu.calculateUsage().entrySet()) {
      TreeSet<String> tableNames = new TreeSet<String>();
      for (String tableId : entry.getKey())
        tableNames.add(reverseTableIdMap.get(tableId));
      
      usage.put(tableNames, entry.getValue());
    }

    if(!emptyTableIds.isEmpty()) {
      TreeSet<String> emptyTables = new TreeSet<String>();
      for (String tableId : emptyTableIds) {
        emptyTables.add(reverseTableIdMap.get(tableId));
      }
      usage.put(emptyTables, 0L);
    }
    
    for (Entry<TreeSet<String>,Long> entry : usage.entrySet())
      printer.print(String.format("%,24d %s", entry.getValue(), entry.getKey()));
    
  }
