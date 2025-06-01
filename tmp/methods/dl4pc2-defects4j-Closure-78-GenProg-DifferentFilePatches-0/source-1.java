  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    currentTraversal.getCompiler().report(error);
  }
  static boolean isCommutative(int type) {
    switch (type) {
      case Token.MUL:
      case Token.BITOR:
      case Token.BITXOR:
      case Token.BITAND:
        return true;
      default:
        return false;
    }
  }
