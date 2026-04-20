  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      validateIsSynthetic(n);
    }
  }
