  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false, false)) {
      return new NodeMismatch(this, node2);
    }

    NodeMismatch res = null;
    Node n, n2;
    for (n = first, n2 = node2.first;
         res == null && n != null;
         n = n.next, n2 = n2.next) {
      if (node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if (res != null) {
        return res;
      }
    }
    return res;
  }
  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        return "Node tree inequality:" +
            "\nTree1:\n" + toStringTree() +
            "\n\nTree2:\n" + node2.toStringTree() +
            "\n\nSubtree1: " + diff.nodeA.toStringTree() +
            "\n\nSubtree2: " + diff.nodeB.toStringTree();
      }
      return null;
  }
  private void replaceWithRhs(Node parent, Node n) {
    if (valueConsumedByParent(n, parent)) {
      // parent reads from n directly; replace it with n's rhs + lhs
      // subexpressions with side effects.
      List<Node> replacements = getRhsSubexpressions(n);
      List<Node> newReplacements = Lists.newArrayList();
      for (int i = 0; i < replacements.size() - 1; i++) {
        newReplacements.addAll(getSideEffectNodes(replacements.get(i)));
      }
      Node valueExpr = replacements.get(replacements.size() - 1);
      valueExpr.detachFromParent();
      newReplacements.add(valueExpr);
      changeProxy.replaceWith(
          parent, n, collapseReplacements(newReplacements));
    } else if (n.isAssign() && !parent.isFor()) {
      // assignment appears in a RHS expression.  we have already
      // considered names in the assignment's RHS as being referenced;
      // replace the assignment with its RHS.
      // TODO(user) make the pass smarter about these cases and/or run
      // this pass and RemoveConstantExpressions together in a loop.
      Node replacement = n.getLastChild();
      replacement.detachFromParent();
      changeProxy.replaceWith(parent, n, replacement);
    } else {
      replaceTopLevelExpressionWithRhs(parent, n);
    }
  }
