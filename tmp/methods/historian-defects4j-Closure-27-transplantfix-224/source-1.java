  public boolean isLabelName() {
    return this.getType() == Token.LABEL_NAME;
  }
  public static Node block(Node stmt) {
    Preconditions.checkState(mayBeStatement(stmt));
    Node block = new Node(Token.BLOCK, stmt);
    return block;
  }
