  static boolean mayBeString(Node n, boolean recurse) {
    if (recurse) {
      return valueCheck(n, MAY_BE_STRING_PREDICATE);
    } else {
      return mayBeStringHelper(n);
    }
  }
  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    currentTraversal.getCompiler().report(error);
  }
