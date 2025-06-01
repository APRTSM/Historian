  public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
    log.setLevel(getLogLevel(job));
    validateOptions(job);

    LinkedList<InputSplit> splits = new LinkedList<InputSplit>();
    Map<String,BatchScanConfig> tableConfigs = getBatchScanConfigs(job);
    for (Map.Entry<String,BatchScanConfig> tableConfigEntry : tableConfigs.entrySet()) {
      String tableName = tableConfigEntry.getKey();
      BatchScanConfig tableConfig = tableConfigEntry.getValue();
      boolean autoAdjust = tableConfig.shouldAutoAdjustRanges();
      String tableId = null;
      List<Range> ranges = autoAdjust ? Range.mergeOverlapping(tableConfig.getRanges()) : tableConfig.getRanges();
      if (ranges.isEmpty()) {
        ranges = new ArrayList<Range>(1);
        ranges.add(new Range());
      }

      // get the metadata information for these ranges
      Map<String,Map<KeyExtent,List<Range>>> binnedRanges = new HashMap<String,Map<KeyExtent,List<Range>>>();
      TabletLocator tl;
      try {
        // resolve table name to id once, and use id from this point forward
        Instance instance = getInstance(job);
        if (instance instanceof MockInstance)
          tableId = "";
        else
          tableId = Tables.getTableId(instance, tableName);
        if (tableConfig.isOfflineScan()) {
          binnedRanges = binOfflineTable(job, tableId, ranges);
          while (binnedRanges == null) {
            // Some tablets were still online, try again
            UtilWaitThread.sleep(100 + (int) (Math.random() * 100)); // sleep randomly between 100 and 200 ms
            binnedRanges = binOfflineTable(job, tableId, ranges);
          }
        } else {
          tl = getTabletLocator(job, tableId);
          // its possible that the cache could contain complete, but old information about a tables tablets... so clear it
          tl.invalidateCache();
          Credentials creds = new Credentials(getPrincipal(job), getAuthenticationToken(job));

          while (!tl.binRanges(creds, ranges, binnedRanges).isEmpty()) {
            if (!(instance instanceof MockInstance)) {
              if (!Tables.exists(instance, tableId))
                throw new TableDeletedException(tableId);
              if (Tables.getTableState(instance, tableId) == TableState.OFFLINE)
                throw new TableOfflineException(instance, tableId);
            }
            binnedRanges.clear();
            log.warn("Unable to locate bins for specified ranges. Retrying.");
            UtilWaitThread.sleep(100 + (int) (Math.random() * 100)); // sleep randomly between 100 and 200 ms
            tl.invalidateCache();
          }
        }
      } catch (Exception e) {
        throw new IOException(e);
      }

      HashMap<Range,ArrayList<String>> splitsToAdd = null;

      if (!autoAdjust)
        splitsToAdd = new HashMap<Range,ArrayList<String>>();

      HashMap<String,String> hostNameCache = new HashMap<String,String>();
      for (Map.Entry<String,Map<KeyExtent,List<Range>>> tserverBin : binnedRanges.entrySet()) {
        String ip = tserverBin.getKey().split(":", 2)[0];
        String location = hostNameCache.get(ip);
        if (location == null) {
          InetAddress inetAddress = InetAddress.getByName(ip);
          location = inetAddress.getHostName();
          hostNameCache.put(ip, location);
        }
        for (Map.Entry<KeyExtent,List<Range>> extentRanges : tserverBin.getValue().entrySet()) {
          Range ke = extentRanges.getKey().toDataRange();
          for (Range r : extentRanges.getValue()) {
            if (autoAdjust) {
              // divide ranges into smaller ranges, based on the tablets
              splits.add(new RangeInputSplit(tableName, tableId, ke.clip(r), new String[] {location}));
            } else {
              // don't divide ranges
              ArrayList<String> locations = splitsToAdd.get(r);
              if (locations == null)
                locations = new ArrayList<String>(1);
              locations.add(location);
              splitsToAdd.put(r, locations);
            }
          }
        }
      }

      if (!autoAdjust)
        for (Map.Entry<Range,ArrayList<String>> entry : splitsToAdd.entrySet())
          splits.add(new RangeInputSplit(tableName, tableId, entry.getKey(), entry.getValue().toArray(new String[0])));
    }

    return splits.toArray(new InputSplit[splits.size()]);
  }
    public void initialize(InputSplit inSplit, JobConf job) throws IOException {
      Scanner scanner;
      split = (RangeInputSplit) inSplit;
      log.debug("Initializing input split: " + split.getRange());
      Instance instance = getInstance(job);
      String principal = getPrincipal(job);
      AuthenticationToken token = getAuthenticationToken(job);
      Authorizations authorizations = getScanAuthorizations(job);

      // in case the table name changed, we can still use the previous name for terms of configuration,
      // but the scanner will use the table id resolved at job setup time
      BatchScanConfig tableConfig = getBatchScanConfig(job, split.getTableName());


      try {
        log.debug("Creating connector with user: " + principal);
        log.debug("Creating scanner for table: " + split.getTableName());
        log.debug("Authorizations are: " + authorizations);
        if (tableConfig.isOfflineScan()) {
          scanner = new OfflineScanner(instance, new Credentials(principal, token), split.getTableId(), authorizations);
        } else if (instance instanceof MockInstance) {
          scanner = instance.getConnector(principal, token).createScanner(split.getTableName(), authorizations);
        } else {
          scanner = new ScannerImpl(instance, new Credentials(principal, token), split.getTableId(), authorizations);
        }
        if (tableConfig.shouldUseIsolatedScanners()) {
          log.info("Creating isolated scanner");
          scanner = new IsolatedScanner(scanner);
        }
        if (tableConfig.shouldUseLocalIterators()) {
          log.info("Using local iterators");
          scanner = new ClientSideIteratorScanner(scanner);
        }
        setupIterators(job, scanner, split.getTableName());
      } catch (Exception e) {
        throw new IOException(e);
      }

      // setup a scanner within the bounds of this split
      for (Pair<Text,Text> c : tableConfig.getFetchedColumns()) {
        if (c.getSecond() != null) {
          log.debug("Fetching column " + c.getFirst() + ":" + c.getSecond());
          scanner.fetchColumn(c.getFirst(), c.getSecond());
        } else {
          log.debug("Fetching column family " + c.getFirst());
          scanner.fetchColumnFamily(c.getFirst());
        }
      }

      scanner.setRange(split.getRange());

      numKeysRead = 0;

      // do this last after setting all scanner options
      scannerIterator = scanner.iterator();
    }
