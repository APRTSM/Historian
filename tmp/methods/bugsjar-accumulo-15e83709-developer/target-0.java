  private synchronized void retry(ZooRunnable op) {

    int sleepTime = 100;

    while (true) {

      ZooKeeper zooKeeper = getZooKeeper();

      try {
        op.run(zooKeeper);
        return;

      } catch (KeeperException e) {
        final Code code = e.code();
        if (code == Code.NONODE) {
          log.error("Looked up non-existent node in cache " + e.getPath(), e);
        } else if (code == Code.CONNECTIONLOSS || code == Code.OPERATIONTIMEOUT || code == Code.SESSIONEXPIRED) {
          log.warn("Saw (possibly) transient exception communicating with ZooKeeper, will retry", e);
          continue;
        }
        log.warn("Zookeeper error, will retry", e);
      } catch (InterruptedException e) {
        log.info("Zookeeper error, will retry", e);
      } catch (ConcurrentModificationException e) {
        log.debug("Zookeeper was modified, will retry");
      }

      try {
        // do not hold lock while sleeping
        wait(sleepTime);
      } catch (InterruptedException e) {
        log.info("Interrupted waiting before retrying ZooKeeper operation", e);
      }
      if (sleepTime < 10000)
        sleepTime = (int) (sleepTime + sleepTime * Math.random());

    }
  }
