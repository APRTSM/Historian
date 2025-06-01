  protected void error(DiagnosticType diagnostic, Node n) {
    Node parameterName = Node.newString(Token.NAME, "jscomp_throw_param");
	JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
  }
