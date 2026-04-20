  public void start() throws IOException, InterruptedException {
    if (zooKeeperProcess != null)
      throw new IllegalStateException("Already started");
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          MiniAccumuloCluster.this.stop();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    
    zooKeeperProcess = exec(Main.class, ZooKeeperServerMain.class.getName(), zooCfgFile.getAbsolutePath());
    
    // sleep a little bit to let zookeeper come up before calling init, seems to work better
    UtilWaitThread.sleep(250);
    
    Process initProcess = exec(Initialize.class, "--instance-name", INSTANCE_NAME, "--password", config.getRootPassword());
    int ret = initProcess.waitFor();
    if (ret != 0) {
      throw new RuntimeException("Initialize process returned " + ret + ". Check the logs in " + logDir + " for errors.");
    }
    
    tabletServerProcesses = new Process[config.getNumTservers()];
    for (int i = 0; i < config.getNumTservers(); i++) {
      tabletServerProcesses[i] = exec(TabletServer.class);
    }
    
    masterProcess = exec(Master.class);
    
    gcProcess = exec(SimpleGarbageCollector.class);
    
    if (null == executor) {
      executor = Executors.newSingleThreadExecutor();
    }
  }
  private int stopProcessWithTimeout(final Process proc, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>() {
      @Override
      public Integer call() throws InterruptedException {
        proc.destroy();
        return proc.waitFor();
      }
    });
    
    executor.execute(future);
    
    return future.get(timeout, unit);
  }
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
