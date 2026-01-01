  protected void error(DiagnosticType diagnostic, Node n) {
    if (n.getType() == Token.NUMBER) {
 double numValue = n.getDouble();
 if (numValue == 0 || numValue == 1) {
 return ;
 }

 }

 JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    currentTraversal.getCompiler().report(error);
  }
