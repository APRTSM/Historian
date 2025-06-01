  public static void assertTypeEquals(String message, JSType a, JSType b) {
    Assert.assertTrue(
        "Both types must be null, or both must be non-null " + a + "," + b,
        (a == null) == (b == null));
    if (a == null) {
      return;
    }
    Assert.assertTrue(
        message +
        (message.isEmpty() ? "" : "\n") +
        "Expected: " + a + "\n" +
        "Actual  : " + b,
        a.isEquivalentTo(b));
    Assert.assertTrue(
        message +
        " Equals is not symmetric.\n" +
        "Expected: " + b + "\n" +
        "Actual  : " + a,
        b.isEquivalentTo(a));
  }
