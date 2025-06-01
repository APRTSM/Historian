  private int updateAuthKeys(String path) throws KeeperException, InterruptedException {
    int keysAdded = 0;
    for (String child : zk.getChildren(path, this)) {
      String childPath = path + "/" + child;
      // Get the node data and reset the watcher
      AuthenticationKey key = deserializeKey(zk.getData(childPath, this, null));
      secretManager.addKey(key);
      keysAdded++;
    }
    return keysAdded;
  }
