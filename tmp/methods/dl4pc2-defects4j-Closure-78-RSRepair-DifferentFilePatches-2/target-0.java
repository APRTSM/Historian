  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    Node parameterName = Node.newString(Token.NAME, "jscomp_throw_param");
  }
