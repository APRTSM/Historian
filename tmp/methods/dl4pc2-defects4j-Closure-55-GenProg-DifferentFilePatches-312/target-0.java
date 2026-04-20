  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      validateNodeType(Token.RETURN, n);
    }
  }
