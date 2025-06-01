  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    currentTraversal.getCompiler().report(error);
  }
  static boolean isNumericResultHelper(Node n) {
    switch (n.getType()) {
      case Token.ADD:
        return !mayBeString(n.getFirstChild())
            && !mayBeString(n.getLastChild());
      case Token.BITNOT:
      case Token.BITOR:
      case Token.BITXOR:
      case Token.BITAND:
      case Token.LSH:
      case Token.RSH:
      case Token.URSH:
      case Token.SUB:
      case Token.MUL:
      case Token.MOD:
      case Token.DIV:
      case Token.INC:
      case Token.DEC:
      case Token.POS:
      case Token.NEG:
      case Token.NUMBER:
        return true;
      case Token.NAME:
        String name = n.getString();
        if (name.equals("NaN")) {
          return true;
        }
        if (name.equals("Infinity")) {
          return true;
        }
        return false;
      default:
        return false;
    }
  }
