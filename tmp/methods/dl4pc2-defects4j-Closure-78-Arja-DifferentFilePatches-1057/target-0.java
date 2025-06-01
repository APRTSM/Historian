  static boolean mayBeString(Node n, boolean recurse) {
    if (recurse) {
      return valueCheck(n, MAY_BE_STRING_PREDICATE);
    } else {
      return false;
    }
  }
