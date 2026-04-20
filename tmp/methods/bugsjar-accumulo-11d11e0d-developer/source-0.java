  public boolean getMigrations(Map<TServerInstance,TabletServerStatus> current, List<TabletMigration> result) {
    boolean moreBalancingNeeded = false;
    try {
      // no moves possible
      if (current.size() < 2) {
        return false;
      }
      final Map<String,Map<KeyExtent,TabletStats>> donerTabletStats = new HashMap<String,Map<KeyExtent,TabletStats>>();
      
      // Sort by total number of online tablets, per server
      int total = 0;
      ArrayList<ServerCounts> totals = new ArrayList<ServerCounts>();
      for (Entry<TServerInstance,TabletServerStatus> entry : current.entrySet()) {
        int serverTotal = 0;
        if (entry.getValue() != null && entry.getValue().tableMap != null) {
          for (Entry<String,TableInfo> e : entry.getValue().tableMap.entrySet()) {
            /**
             * The check below was on entry.getKey(), but that resolves to a tabletserver not a tablename. Believe it should be e.getKey() which is a tablename
             */
            if (tableToBalance == null || tableToBalance.equals(e.getKey()))
              serverTotal += e.getValue().onlineTablets;
          }
        }
        totals.add(new ServerCounts(serverTotal, entry.getKey(), entry.getValue()));
        total += serverTotal;
      }
      
      // order from low to high
      Collections.sort(totals);
      Collections.reverse(totals);
      int even = total / totals.size();
      int numServersOverEven = total % totals.size();
      
      // Move tablets from the servers with too many to the servers with
      // the fewest but only nominate tablets to move once. This allows us
      // to fill new servers with tablets from a mostly balanced server
      // very quickly. However, it may take several balancing passes to move
      // tablets from one hugely overloaded server to many slightly
      // under-loaded servers.
      int end = totals.size() - 1;
      int movedAlready = 0;
      int tooManyIndex = 0;
      while (tooManyIndex < totals.size() && end > tooManyIndex) {
        ServerCounts tooMany = totals.get(tooManyIndex);
        int goal = even;
        if (tooManyIndex < numServersOverEven) {
          goal++;
        }
        int needToUnload = tooMany.count - goal;
        ServerCounts tooLittle = totals.get(end);
        int needToLoad = goal - tooLittle.count - movedAlready;
        if (needToUnload < 1 && needToLoad < 1) {
          break;
        }
        if (needToUnload >= needToLoad) {
          result.addAll(move(tooMany, tooLittle, needToLoad, donerTabletStats));
          end--;
          movedAlready = 0;
        } else {
          result.addAll(move(tooMany, tooLittle, needToUnload, donerTabletStats));
          movedAlready += needToUnload;
        }
        if (needToUnload > needToLoad) {
          moreBalancingNeeded = true;
        } else {
          tooManyIndex++;
          donerTabletStats.clear();
        }
      }
      
    } finally {
      log.debug("balance ended with " + result.size() + " migrations");
    }
    return moreBalancingNeeded;
  }
  List<TabletMigration> move(ServerCounts tooMuch, ServerCounts tooLittle, int count, Map<String,Map<KeyExtent,TabletStats>> donerTabletStats) {
    
    List<TabletMigration> result = new ArrayList<TabletMigration>();
    if (count == 0)
      return result;
    
    // Copy counts so we can update them as we propose migrations
    Map<String,Integer> tooMuchMap = tabletCountsPerTable(tooMuch.status);
    Map<String,Integer> tooLittleMap = tabletCountsPerTable(tooLittle.status);
    
    for (int i = 0; i < count; i++) {
      String table;
      Integer tooLittleCount;
      if (tableToBalance == null) {
        // find a table to migrate
        // look for an uneven table count
        int biggestDifference = 0;
        String biggestDifferenceTable = null;
        for (Entry<String,Integer> tableEntry : tooMuchMap.entrySet()) {
          String tableID = tableEntry.getKey();
          if (tooLittleMap.get(tableID) == null)
            tooLittleMap.put(tableID, 0);
          int diff = tableEntry.getValue() - tooLittleMap.get(tableID);
          if (diff > biggestDifference) {
            biggestDifference = diff;
            biggestDifferenceTable = tableID;
          }
        }
        if (biggestDifference < 2) {
          table = busiest(tooMuch.status.tableMap);
        } else {
          table = biggestDifferenceTable;
        }
      } else {
        // just balance the given table
        table = tableToBalance;
      }
      Map<KeyExtent,TabletStats> onlineTabletsForTable = donerTabletStats.get(table);
      try {
        if (onlineTabletsForTable == null) {
          onlineTabletsForTable = new HashMap<KeyExtent,TabletStats>();
          for (TabletStats stat : getOnlineTabletsForTable(tooMuch.server, table))
            onlineTabletsForTable.put(new KeyExtent(stat.extent), stat);
          donerTabletStats.put(table, onlineTabletsForTable);
        }
      } catch (Exception ex) {
        log.error("Unable to select a tablet to move", ex);
        return result;
      }
      KeyExtent extent = selectTablet(tooMuch.server, onlineTabletsForTable);
      onlineTabletsForTable.remove(extent);
      if (extent == null)
        return result;
      tooMuchMap.put(table, tooMuchMap.get(table) - 1);
      /**
       * If a table grows from 1 tablet then tooLittleMap.get(table) can return a null, since there is only one tabletserver that holds all of the tablets. Here
       * we check to see if in fact that is the case and if so set the value to 0.
       */
      tooLittleCount = tooLittleMap.get(table);
      if (tooLittleCount == null) {
        tooLittleCount = 0;
      }
      tooLittleMap.put(table, tooLittleCount + 1);
      
      result.add(new TabletMigration(extent, tooMuch.server, tooLittle.server));
    }
    return result;
  }
