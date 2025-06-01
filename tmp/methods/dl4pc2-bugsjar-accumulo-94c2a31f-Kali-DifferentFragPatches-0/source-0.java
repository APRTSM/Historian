  public void stop() throws IOException, InterruptedException {
    if (zooKeeperProcess != null) {
      try {
        stopProcessWithTimeout(zooKeeperProcess, 30, TimeUnit.SECONDS);
      } catch (ExecutionException e) {
        log.warn("ZooKeeper did not fully stop after 30 seconds", e);
      } catch (TimeoutException e) {
        log.warn("ZooKeeper did not fully stop after 30 seconds", e);
      }
    }
    if (masterProcess != null) {
      try {
        stopProcessWithTimeout(masterProcess, 30, TimeUnit.SECONDS);
      } catch (ExecutionException e) {
        log.warn("Master did not fully stop after 30 seconds", e);
      } catch (TimeoutException e) {
        log.warn("Master did not fully stop after 30 seconds", e);
      }
    }
    if (tabletServerProcesses != null) {
      for (Process tserver : tabletServerProcesses) {
        try {
          stopProcessWithTimeout(tserver, 30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
          log.warn("TabletServer did not fully stop after 30 seconds", e);
        } catch (TimeoutException e) {
          log.warn("TabletServer did not fully stop after 30 seconds", e);
        }
      }
    }
    
    for (LogWriter lw : logWriters)
      lw.flush();
    
    if (gcProcess != null) {
      try {
        stopProcessWithTimeout(gcProcess, 30, TimeUnit.SECONDS);
      } catch (ExecutionException e) {
        log.warn("GarbageCollector did not fully stop after 30 seconds", e);
      } catch (TimeoutException e) {
        log.warn("GarbageCollector did not fully stop after 30 seconds", e);
      }
    }
    
    // ACCUMULO-2985 stop the ExecutorService after we finished using it to stop accumulo procs
    if (null != executor) {
      List<Runnable> tasksRemaining = executor.shutdownNow();
      
      // the single thread executor shouldn't have any pending tasks, but check anyways
      if (!tasksRemaining.isEmpty()) {
        log.warn("Unexpectedly had " + tasksRemaining.size() + " task(s) remaining in threadpool for execution when being stopped");
      }
      
      executor = null;
    }
  }
