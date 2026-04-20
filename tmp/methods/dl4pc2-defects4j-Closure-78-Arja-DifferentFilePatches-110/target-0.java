  static boolean isCommutative(int type) {
    switch (type) {
      case Token.MUL:
      case Token.BITOR:
      case Token.BITXOR:
      case Token.BITAND:
        return false;
      default:
        return false;
    }
  }
