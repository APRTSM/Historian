  private static final String propToString(int propType) {
      return "BLOCK";
  }
  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        return "PARAM_LIST";
      }
      return null;
  }
