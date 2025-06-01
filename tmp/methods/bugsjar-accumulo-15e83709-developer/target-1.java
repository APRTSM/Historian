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
  public static boolean putPersistentData(ZooKeeperConnectionInfo info, String zPath, byte[] data, int version, NodeExistsPolicy policy)
      throws KeeperException, InterruptedException {
    return putData(info, zPath, data, CreateMode.PERSISTENT, version, policy, PUBLIC);
  }
  private static boolean putData(ZooKeeperConnectionInfo info, String zPath, byte[] data, CreateMode mode, int version, NodeExistsPolicy policy, List<ACL> acls)
      throws KeeperException, InterruptedException {
    if (policy == null)
      policy = NodeExistsPolicy.FAIL;

    final Retry retry = RETRY_FACTORY.create();
    while (true) {
      try {
        getZooKeeper(info).create(zPath, data, acls, mode);
        return true;
      } catch (KeeperException e) {
        final Code code = e.code();
        if (code == Code.NODEEXISTS) {
          switch (policy) {
            case SKIP:
              return false;
            case OVERWRITE:
              // overwrite the data in the node when it already exists
              try {
                getZooKeeper(info).setData(zPath, data, version);
                return true;
              } catch (KeeperException e2) {
                final Code code2 = e2.code();
                if (code2 == Code.NONODE) {
                  // node delete between create call and set data, so try create call again
                  continue;
                } else if (code2 == Code.CONNECTIONLOSS || code2 == Code.OPERATIONTIMEOUT || code2 == Code.SESSIONEXPIRED) {
                  retryOrThrow(retry, e2);
                } else {
                  // unhandled exception on setData()
                  throw e2;
                }
              }
            default:
              throw e;
          }
        } else if (code == Code.CONNECTIONLOSS || code == Code.OPERATIONTIMEOUT || code == Code.SESSIONEXPIRED) {
          retryOrThrow(retry, e);
        } else {
          // unhandled exception on create()
          throw e;
        }
      }

      // Catch all to wait before retrying
      retry.waitForNextAttempt();
    }
  }
    public String toString() {
      StringBuilder sb = new StringBuilder(64);
      sb.append("zookeepers=").append(keepers);
      sb.append(", timeout=").append(timeout);
      sb.append(", scheme=").append(scheme);
      sb.append(", auth=").append(null == auth ? "null" : "REDACTED");
      return sb.toString();
    }
    public ZooKeeperConnectionInfo(String keepers, int timeout, String scheme, byte[] auth) {
      Preconditions.checkNotNull(keepers);
      this.keepers = keepers;
      this.timeout = timeout;
      this.scheme = scheme;
      this.auth = auth;
    }
    public boolean equals(Object o) {
      if (o instanceof ZooKeeperConnectionInfo) {
        ZooKeeperConnectionInfo other = (ZooKeeperConnectionInfo) o;
        if (!keepers.equals(other.keepers) || timeout != other.timeout) {
          return false;
        }

        if (null != scheme) {
          if (null == other.scheme) {
            // Ours is non-null, theirs is null
            return false;
          } else if (!scheme.equals(other.scheme)) {
            // Both non-null but not equal
            return false;
          }
        }

        if (null != auth) {
          if (null == other.auth) {
            return false;
          } else if (!Arrays.equals(auth, other.auth)) {
            // both non-null but not equal
            return false;
          }
        }

        return true;
      }

      return false;
    }
  public static void recursiveCopyPersistent(ZooKeeperConnectionInfo info, String source, String destination, NodeExistsPolicy policy) throws KeeperException,
      InterruptedException {
    Stat stat = null;
    if (!exists(info, source))
      throw KeeperException.create(Code.NONODE, source);
    if (exists(info, destination)) {
      switch (policy) {
        case OVERWRITE:
          break;
        case SKIP:
          return;
        case FAIL:
        default:
          throw KeeperException.create(Code.NODEEXISTS, source);
      }
    }

    stat = new Stat();
    byte[] data = getData(info, source, stat);

    if (stat.getEphemeralOwner() == 0) {
      if (data == null)
        throw KeeperException.create(Code.NONODE, source);
      putPersistentData(info, destination, data, policy);
      if (stat.getNumChildren() > 0) {
        List<String> children;
        final Retry retry = RETRY_FACTORY.create();
        while (true) {
          try {
            children = getZooKeeper(info).getChildren(source, false);
            break;
          } catch (KeeperException e) {
            final Code c = e.code();
            if (c == Code.CONNECTIONLOSS || c == Code.OPERATIONTIMEOUT || c == Code.SESSIONEXPIRED) {
              retryOrThrow(retry, e);
            } else {
              throw e;
            }
          }
          retry.waitForNextAttempt();
        }
        for (String child : children) {
          recursiveCopyPersistent(info, source + "/" + child, destination + "/" + child, policy);
        }
      }
    }
  }
    public int hashCode() {
      final HashCodeBuilder hcb = new HashCodeBuilder(31, 47);
      hcb.append(keepers).append(timeout);
      if (null != scheme) {
        hcb.append(scheme);
      }
      if (null != auth) {
        hcb.append(auth);
      }
      return hcb.toHashCode();
    }
