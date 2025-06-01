  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      return false;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
  CheckGlobalNames injectNamespace(GlobalNamespace namespace) {
    this.namespace = namespace;
    return null;
  }
