  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      return false;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
    protected void add(CheckLevel level, String groupName) {
      return;
    }
