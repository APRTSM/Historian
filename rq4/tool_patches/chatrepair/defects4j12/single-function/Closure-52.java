static boolean isSimpleNumber(String s) {
  if ((s.length() > 1) && (s.charAt(0) == '0')) {
    return false;
  }
  for (int index = 0; index < s.length(); index++) {
    char c = s.charAt(index);
    if (c < '0' || c > '9') {
      return false;
    }
  }
  return s.length() > 0;
}
