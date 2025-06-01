  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    int type = n.getType();
  }
  static boolean mayBeString(Node n, boolean recurse) {
    if (recurse) {
      return valueCheck(n, MAY_BE_STRING_PREDICATE);
    } else {
      int index = -1;
	return mayBeStringHelper(n);
    }
  }
