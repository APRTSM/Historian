  static String trimJsWhiteSpace(String s) {
    int start = 0;
    int end = s.length();
    while (end > 0
        && isStrWhiteSpaceChar(s.charAt(end - 1)) == TernaryValue.TRUE) {
      end--;
    }
    while (start < end
        && isStrWhiteSpaceChar(s.charAt(start)) == TernaryValue.TRUE) {
      start++;
    }
    return s.substring(start, end);
  }
