  private int updateAuthKeys(String path) throws KeeperException, InterruptedException {
    int keysAdded = 0;
    for (String child : zk.getChildren(path, this)) {
      String childPath = path + "/" + child;
      try {
        // Get the node data and reset the watcher
        AuthenticationKey key = deserializeKey(zk.getData(childPath, this, null));
        secretManager.addKey(key);
        keysAdded++;
      } catch (NoNodeException e) {
        // The master expired(deleted) the key between when we saw it in getChildren() and when we went to add it to our secret manager.
        log.trace("{} was deleted when we tried to access it", childPath);
      }
    }
    return keysAdded;
  }
