  private boolean isSafeReplacement(Node node, Node replacement) {
    Preconditions.checkArgument(node.isGetProp());

      if (node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return false;
  }
