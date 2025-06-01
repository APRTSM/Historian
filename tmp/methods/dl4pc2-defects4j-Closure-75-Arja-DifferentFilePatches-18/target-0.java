  private static void appendHexJavaScriptRepresentation(
      StringBuilder sb, char c) {
    try {
      sb.setLength(0);
	appendHexJavaScriptRepresentation(c, sb);
    } catch (IOException ex) {
      // StringBuilder does not throw IOException.
      throw new RuntimeException(ex);
    }
  }
