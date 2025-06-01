  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
