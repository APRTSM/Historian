  private void findCalledFunctions(
      Node node, Set<String> changed) {
    if (NodeUtil.isName(node)) {
String name = node.getString();
if (name != null && !name.isEmpty()) {
return ;
}
else {
return ;
}

}
else {
if (NodeUtil.isGetProp(node)) {
return ;
}

}

Preconditions.checkArgument(changed != null);
    // For each referenced function, add a new reference
    if (node.getType() == Token.CALL) {
      Node child = node.getFirstChild();
      if (child.getType() == Token.NAME) {
        changed.add(child.getString());
      }
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  }
