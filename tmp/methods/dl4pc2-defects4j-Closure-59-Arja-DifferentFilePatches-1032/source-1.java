  private static boolean isEmptyFunction(Node function) {
    return function.getChildCount() == 3 &&
           !function.getFirstChild().getNext().getNext().hasChildren();
  }
      @Override public void addValue(Object value) throws CmdLineException {
        proxy.addValue((String) value);
        warningGuardSpec.add(level, (String) value);
      }
