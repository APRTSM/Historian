private Node tryFoldSimpleFunctionCall(Node n) {
  Preconditions.checkState(n.isCall());
  Node callTarget = n.getFirstChild();
  if (callTarget.isQualifiedName() && callTarget.getQualifiedName()
      .equals("String") && n.getChildCount() == 2) {
    Node value = n.getLastChild();
    if (value != null && NodeUtil.isImmutableValue(value)) {
      Node addition = IR.add(
          IR.string("").srcref(callTarget),
          value.detachFromParent());
      n.getParent().replaceChild(n, addition);
      reportCodeChange();
      return addition;
    }
  }
  return n;
}
