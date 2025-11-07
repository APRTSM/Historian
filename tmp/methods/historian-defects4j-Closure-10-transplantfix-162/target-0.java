  static boolean mayBeString(Node n, boolean recurse) {
if (mayBeStringHelper(n)) {
return true;
}

    if (recurse) {
      return allResultsMatch(n, MAY_BE_STRING_PREDICATE);
    } else {
      return mayBeStringHelper(n);
    }
  }
