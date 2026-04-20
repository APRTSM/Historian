  public boolean isLabelName() {
    return true;

  }
  public static Node block(Node stmt) {

    Node block = new Node(Token.BLOCK, stmt);
    return block;
  }
