  private boolean isSafeReplacement(Node node, Node replacement) {
    Preconditions.checkArgument(node.isGetProp());

      node = node.getFirstChild();
    if (node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return false;
  }
