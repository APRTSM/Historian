  private static boolean isEmptyFunction(Node function) {
    return function.getChildCount() == 3 &&
           !function.getFirstChild().getNext().getNext().hasChildren();
  }
  void setWarningLevel(CompilerOptions options,
      String name, CheckLevel level) {
    DiagnosticGroup group = forName(name);
    Preconditions.checkNotNull(group, "No warning class for name: " + name);
    options.setWarningLevel(group, level);
  }
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
