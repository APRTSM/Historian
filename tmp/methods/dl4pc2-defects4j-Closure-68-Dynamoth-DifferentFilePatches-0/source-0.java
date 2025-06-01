  public void warning(String message, String sourceName, int line,
      String lineSource, int lineOffset) {
    if (warnings != null && warningsIndex < warnings.length) {
      assertEquals(warnings[warningsIndex++], message);
    } else {
      fail("extra warning: " + message);
    }
  }
