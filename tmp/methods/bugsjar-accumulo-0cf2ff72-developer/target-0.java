  public void start() throws IOException, InterruptedException {
    
    if (!initialized) {
      
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
    }
    
    if (zooKeeperProcess == null) {
      zooKeeperProcess = exec(Main.class, ServerType.ZOOKEEPER, ZooKeeperServerMain.class.getName(), zooCfgFile.getAbsolutePath());
    }
    
    if (!initialized) {
      // sleep a little bit to let zookeeper come up before calling init, seems to work better
      UtilWaitThread.sleep(250);
      Process initProcess = exec(Initialize.class, "--instance-name", config.getInstanceName(), "--password", config.getRootPassword());
      int ret = initProcess.waitFor();
      if (ret != 0) {
        throw new RuntimeException("Initialize process returned " + ret);
      }
      initialized = true;
    }
    
    for (int i = tabletServerProcesses.size(); i < config.getNumTservers(); i++) {
      tabletServerProcesses.add(exec(TabletServer.class, ServerType.TABLET_SERVER));
    }
    int ret = 0;
    for (int i = 0; i < 5; i++) {
      ret = exec(Main.class, SetGoalState.class.getName(), MasterGoalState.NORMAL.toString()).waitFor();
      if (ret == 0)
        break;
      UtilWaitThread.sleep(1000);
    }
    if (ret != 0) {
      throw new RuntimeException("Could not set master goal state, process returned " + ret);
    }
    if (masterProcess == null) {
      masterProcess = exec(Master.class, ServerType.MASTER);
    }
  }
