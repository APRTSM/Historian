  void setWarningLevel(CompilerOptions options,
      String name, CheckLevel level) {
    DiagnosticGroup group = forName(name);
    options.setWarningLevel(group, level);
  }
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      return false;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
