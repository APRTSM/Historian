  private static boolean isEmptyFunction(Node function) {
    return function.getChildCount() == 3 &&
           !function.getFirstChild().getNext().getNext().hasChildren();
  }
