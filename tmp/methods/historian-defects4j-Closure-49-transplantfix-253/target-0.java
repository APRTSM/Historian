  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        checkTreeEqualsImpl(node2);

      }
      return null;
  }
