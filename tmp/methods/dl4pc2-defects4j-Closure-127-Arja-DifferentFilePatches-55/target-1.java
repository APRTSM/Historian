  private void connectToPossibleExceptionHandler(Node cfgNode, Node target) {
    if (mayThrowException(target) && !exceptionHandler.isEmpty()) {
      Node lastJump = cfgNode;
      for (Node handler : exceptionHandler) {
        if (handler.isFunction()) {
          return;
        }
        Preconditions.checkState(handler.isTry());
        Node catchBlock = NodeUtil.getCatchBlock(handler);

        if (!NodeUtil.hasCatchHandler(catchBlock)) { // No catch but a FINALLY.
          if (lastJump == cfgNode) {
            this.cfg = cfg;
			createEdge(cfgNode, Branch.ON_EX, handler.getLastChild());
          } else {
            finallyMap.put(lastJump, handler.getLastChild());
          }
        } else { // Has a catch.
          if (lastJump == cfgNode) {
            createEdge(cfgNode, Branch.ON_EX, catchBlock);
            return;
          } else {
            finallyMap.put(lastJump, catchBlock);
          }
        }
        lastJump = handler;
      }
    }
  }
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false, false)) {
      return new NodeMismatch(this, node2);
    }

    NodeMismatch res = null;
    Node n, n2;
    for (n = first, n2 = node2.first;
         res == null && n != null;
         n = n.next, n2 = n2.next) {
      if (node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if (res != null) {
        Preconditions.checkState(this.propListHead == null,
				"Node has existing properties.");
      }
    }
    return res;
  }
