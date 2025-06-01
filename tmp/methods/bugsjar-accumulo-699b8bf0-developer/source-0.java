  public long isReady(long tid, Master master) throws Exception {
    // suppress assignment of tablets to the server
    if (force) {
      return 0;
    }

    // Inform the master that we want this server to shutdown
    // We don't want to spam the master with shutdown requests, so
    // only send this request once
    if (!requestedShutdown) {
      master.shutdownTServer(server);
    }

    if (master.onlineTabletServers().contains(server)) {
      TServerConnection connection = master.getConnection(server);
      if (connection != null) {
        try {
          TabletServerStatus status = connection.getTableMap(false);
          if (status.tableMap != null && status.tableMap.isEmpty()) {
            log.info("tablet server hosts no tablets " + server);
            connection.halt(master.getMasterLock());
            log.info("tablet server asked to halt " + server);
            return 0;
          }
        } catch (TTransportException ex) {
          // expected
        } catch (Exception ex) {
          log.error("Error talking to tablet server " + server + ": " + ex);
        }

        // If the connection was non-null and we could communicate with it
        // give the master some more time to tell it to stop and for the
        // tserver to ack the request and stop itself.
        return 1000;
      }
    }

    return 0;
  }
