  public void printSummary() {
    if (summaryDetailLevel >= 3 ||
        (summaryDetailLevel >= 1 && getErrorCount() + getWarningCount() > 0) ||
        (summaryDetailLevel >= 2 && getTypedPercent() > 0.0)) {
      if (getTypedPercent() > 0.0) {
        stream.format("%d error(s), %d warning(s), %.1f%% typed%n",
            getErrorCount(), getWarningCount(), getTypedPercent());
      } else {
        stream.format("%d error(s), %d warning(s)%n", getErrorCount(),
            getWarningCount());
      }
    }
  }
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
