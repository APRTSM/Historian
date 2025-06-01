  public static List<TabletLocation> findOverlappingTablets(AccumuloConfiguration acuConf, FileSystem fs, TabletLocator locator, Path file, Text startRow,
      Text endRow) throws Exception {
    List<TabletLocation> result = new ArrayList<TabletLocation>();
    Collection<ByteSequence> columnFamilies = Collections.emptyList();
    String filename = file.toString();
    // log.debug(filename + " finding overlapping tablets " + startRow + " -> " + endRow);
    FileSKVIterator reader = FileOperations.getInstance().openReader(filename, true, fs, fs.getConf(), acuConf);
    try {
      Text row = startRow;
      if (row == null)
        row = new Text();
      while (true) {
        // log.debug(filename + " Seeking to row " + row);
        reader.seek(new Range(row, null), columnFamilies, false);
        if (!reader.hasTop()) {
          // log.debug(filename + " not found");
          break;
        }
        row = reader.getTopKey().getRow();
        TabletLocation tabletLocation = locator.locateTablet(row, false, true);
        // log.debug(filename + " found row " + row + " at location " + tabletLocation);
        result.add(tabletLocation);
        row = tabletLocation.tablet_extent.getEndRow();
        if (row != null && (endRow == null || row.compareTo(endRow) < 0)) {
          row = new Text(row);
          row.append(byte0, 0, byte0.length);
        } else
          break;
      }
    } finally {
      reader.close();
    }
    // log.debug(filename + " to be sent to " + result);
    return result;
  }
  public AssignmentStats importFiles(List<String> files, Path failureDir) throws IOException, AccumuloException, AccumuloSecurityException,
      ThriftTableOperationException {
    
    int numThreads = acuConf.getCount(Property.TSERV_BULK_PROCESS_THREADS);
    int numAssignThreads = acuConf.getCount(Property.TSERV_BULK_ASSIGNMENT_THREADS);
    
    timer = new StopWatch<Timers>(Timers.class);
    timer.start(Timers.TOTAL);
    
    Configuration conf = CachedConfiguration.getInstance();
    final FileSystem fs = FileSystem.get(conf);
    
    Set<Path> paths = new HashSet<Path>();
    for (String file : files) {
      paths.add(new Path(file));
    }
    AssignmentStats assignmentStats = new AssignmentStats(paths.size());
    
    final Map<Path,List<KeyExtent>> completeFailures = Collections.synchronizedSortedMap(new TreeMap<Path,List<KeyExtent>>());
    
    if (!fs.exists(failureDir)) {
      log.error(failureDir + " does not exist");
      throw new RuntimeException("Directory does not exist " + failureDir);
    }
    
    ClientService.Iface client = null;
    final TabletLocator locator = TabletLocator.getInstance(instance, credentials, new Text(tableId));
    
    try {
      final Map<Path,List<TabletLocation>> assignments = Collections.synchronizedSortedMap(new TreeMap<Path,List<TabletLocation>>());
      
      timer.start(Timers.EXAMINE_MAP_FILES);
      ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
      
      for (Path path : paths) {
        final Path mapFile = path;
        Runnable getAssignments = new Runnable() {
          public void run() {
            List<TabletLocation> tabletsToAssignMapFileTo = Collections.emptyList();
            try {
              tabletsToAssignMapFileTo = findOverlappingTablets(instance.getConfiguration(), fs, locator, mapFile);
            } catch (Exception ex) {
              log.warn("Unable to find tablets that overlap file " + mapFile.toString());
            }
            log.debug("Map file " + mapFile + " found to overlap " + tabletsToAssignMapFileTo.size() + " tablets");
            if (tabletsToAssignMapFileTo.size() == 0) {
              List<KeyExtent> empty = Collections.emptyList();
              completeFailures.put(mapFile, empty);
            } else
              assignments.put(mapFile, tabletsToAssignMapFileTo);
          }
        };
        threadPool.submit(new TraceRunnable(new LoggingRunnable(log, getAssignments)));
      }
      threadPool.shutdown();
      while (!threadPool.isTerminated()) {
        try {
          threadPool.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      timer.stop(Timers.EXAMINE_MAP_FILES);
      
      assignmentStats.attemptingAssignments(assignments);
      Map<Path,List<KeyExtent>> assignmentFailures = assignMapFiles(acuConf, instance, conf, credentials, fs, tableId, assignments, paths, numAssignThreads,
          numThreads);
      assignmentStats.assignmentsFailed(assignmentFailures);
      
      Map<Path,Integer> failureCount = new TreeMap<Path,Integer>();
      
      for (Entry<Path,List<KeyExtent>> entry : assignmentFailures.entrySet())
        failureCount.put(entry.getKey(), 1);
      
      while (assignmentFailures.size() > 0) {
        locator.invalidateCache();
        // assumption about assignment failures is that it caused by a split
        // happening or a missing location
        //
        // for splits we need to find children key extents that cover the
        // same key range and are contiguous (no holes, no overlap)
        
        timer.start(Timers.SLEEP);
        UtilWaitThread.sleep(4000);
        timer.stop(Timers.SLEEP);
        
        log.debug("Trying to assign " + assignmentFailures.size() + " map files that previously failed on some key extents");
        assignments.clear();
        
        // for failed key extents, try to find children key extents to
        // assign to
        for (Entry<Path,List<KeyExtent>> entry : assignmentFailures.entrySet()) {
          Iterator<KeyExtent> keListIter = entry.getValue().iterator();
          
          List<TabletLocation> tabletsToAssignMapFileTo = new ArrayList<TabletLocation>();
          
          while (keListIter.hasNext()) {
            KeyExtent ke = keListIter.next();
            
            try {
              timer.start(Timers.QUERY_METADATA);
              tabletsToAssignMapFileTo.addAll(findOverlappingTablets(instance.getConfiguration(), fs, locator, entry.getKey(), ke));
              timer.stop(Timers.QUERY_METADATA);
              keListIter.remove();
            } catch (Exception ex) {
              log.warn("Exception finding overlapping tablets, will retry tablet " + ke);
            }
          }
          
          if (tabletsToAssignMapFileTo.size() > 0)
            assignments.put(entry.getKey(), tabletsToAssignMapFileTo);
        }
        
        assignmentStats.attemptingAssignments(assignments);
        Map<Path,List<KeyExtent>> assignmentFailures2 = assignMapFiles(acuConf, instance, conf, credentials, fs, tableId, assignments, paths, numAssignThreads,
            numThreads);
        assignmentStats.assignmentsFailed(assignmentFailures2);
        
        // merge assignmentFailures2 into assignmentFailures
        for (Entry<Path,List<KeyExtent>> entry : assignmentFailures2.entrySet()) {
          assignmentFailures.get(entry.getKey()).addAll(entry.getValue());
          
          Integer fc = failureCount.get(entry.getKey());
          if (fc == null)
            fc = 0;
          
          failureCount.put(entry.getKey(), fc + 1);
        }
        
        // remove map files that have no more key extents to assign
        Iterator<Entry<Path,List<KeyExtent>>> afIter = assignmentFailures.entrySet().iterator();
        while (afIter.hasNext()) {
          Entry<Path,List<KeyExtent>> entry = afIter.next();
          if (entry.getValue().size() == 0)
            afIter.remove();
        }
        
        Set<Entry<Path,Integer>> failureIter = failureCount.entrySet();
        for (Entry<Path,Integer> entry : failureIter) {
          if (entry.getValue() > acuConf.getCount(Property.TSERV_BULK_RETRY) && assignmentFailures.get(entry.getKey()) != null) {
            log.error("Map file " + entry.getKey() + " failed more than three times, giving up.");
            completeFailures.put(entry.getKey(), assignmentFailures.get(entry.getKey()));
            assignmentFailures.remove(entry.getKey());
          }
        }
      }
      assignmentStats.assignmentsAbandoned(completeFailures);
      Set<Path> failedFailures = processFailures(conf, fs, failureDir, completeFailures);
      assignmentStats.unrecoveredMapFiles(failedFailures);
      
      timer.stop(Timers.TOTAL);
      printReport();
      return assignmentStats;
    } finally {
      if (client != null)
        ServerClient.close(client);
      locator.invalidateCache();
    }
  }
