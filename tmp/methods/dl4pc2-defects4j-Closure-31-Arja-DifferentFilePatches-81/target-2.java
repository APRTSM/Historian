  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
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
        return null;
      }
    }
    return res;
  }
  private static void applyBasicCompilationOptions(CompilerOptions options) {
    // Allows annotations that are not standard.
    options.setWarningLevel(DiagnosticGroups.NON_STANDARD_JSDOC,
        CheckLevel.OFF);
  }
      public int parseArguments(Parameters params) throws CmdLineException {
        String param = null;
        try {
          param = params.getParameter(0);
        } catch (CmdLineException e) {}

        if (param == null) {
          setter.addValue(true);
          return 0;
        } else {
          String lowerParam = param.toLowerCase();
          if (TRUES.contains(lowerParam)) {
            setter.addValue(true);
          } else if (FALSES.contains(lowerParam)) {
            setter.addValue(false);
          } else {
            setter.addValue(true);
          }
          return 1;
        }
      }
