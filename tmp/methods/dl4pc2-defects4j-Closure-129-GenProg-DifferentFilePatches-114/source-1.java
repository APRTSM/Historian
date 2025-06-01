  static boolean isExpressionResultUsed(Node expr) {
    // TODO(johnlenz): consider sharing some code with trySimpleUnusedResult.
    Node parent = expr.getParent();
    switch (parent.getType()) {
      case Token.BLOCK:
      case Token.EXPR_RESULT:
        return false;
      case Token.CAST:
        return isExpressionResultUsed(parent);
      case Token.HOOK:
      case Token.AND:
      case Token.OR:
        return (expr == parent.getFirstChild())
            ? true : isExpressionResultUsed(parent);
      case Token.COMMA:
        Node gramps = parent.getParent();
        if (gramps.isCall() &&
            parent == gramps.getFirstChild()) {
          // Semantically, a direct call to eval is different from an indirect
          // call to an eval. See ECMA-262 S15.1.2.1. So it's OK for the first
          // expression to a comma to be a no-op if it's used to indirect
          // an eval. This we pretend that this is "used".
          if (expr == parent.getFirstChild() &&
              parent.getChildCount() == 2 &&
              expr.getNext().isName() &&
              "eval".equals(expr.getNext().getString())) {
            return true;
          }
        }

        return (expr == parent.getFirstChild())
            ? false : isExpressionResultUsed(parent);
      case Token.FOR:
        if (!NodeUtil.isForIn(parent)) {
          // Only an expression whose result is in the condition part of the
          // expression is used.
          return (parent.getChildAtIndex(1) == expr);
        }
        break;
    }
    return true;
  }
  private Node tryFoldArrayJoin(Node n) {
    Node callTarget = n.getFirstChild();

    if (callTarget == null || !callTarget.isGetProp()) {
      return n;
    }

    Node right = callTarget.getNext();
    if (right != null) {
      if (right.getNext() != null || !NodeUtil.isImmutableValue(right)) {
        return n;
      }
    }

    Node arrayNode = callTarget.getFirstChild();
    Node functionName = arrayNode.getNext();

    if (!arrayNode.isArrayLit() ||
        !functionName.getString().equals("join")) {
      return n;
    }

    if (right != null && right.isString()
        && ",".equals(right.getString())) {
      // "," is the default, it doesn't need to be explicit
      n.removeChild(right);
      reportCodeChange();
    }

    String joinString = (right == null) ? "," : NodeUtil.getStringValue(right);
    List<Node> arrayFoldedChildren = Lists.newLinkedList();
    StringBuilder sb = null;
    int foldedSize = 0;
    Node prev = null;
    Node elem = arrayNode.getFirstChild();
    // Merges adjacent String nodes.
    while (elem != null) {
      if (NodeUtil.isImmutableValue(elem) || elem.isEmpty()) {
        if (sb == null) {
          sb = new StringBuilder();
        } else {
          sb.append(joinString);
        }
        sb.append(NodeUtil.getArrayElementStringValue(elem));
      } else {
        if (sb != null) {
          Preconditions.checkNotNull(prev);
          // + 2 for the quotes.
          foldedSize += sb.length() + 2;
          arrayFoldedChildren.add(
              IR.string(sb.toString()).copyInformationFrom(prev));
          sb = null;
        }
        foldedSize += InlineCostEstimator.getCost(elem);
        arrayFoldedChildren.add(elem);
      }
      prev = elem;
      elem = elem.getNext();
    }

    if (sb != null) {
      Preconditions.checkNotNull(prev);
      // + 2 for the quotes.
      foldedSize += sb.length() + 2;
      arrayFoldedChildren.add(
          IR.string(sb.toString()).copyInformationFrom(prev));
    }
    // one for each comma.
    foldedSize += arrayFoldedChildren.size() - 1;

    int originalSize = InlineCostEstimator.getCost(n);
    switch (arrayFoldedChildren.size()) {
      case 0:
        Node emptyStringNode = IR.string("");
        n.getParent().replaceChild(n, emptyStringNode);
        reportCodeChange();
        return emptyStringNode;
      case 1:
        Node foldedStringNode = arrayFoldedChildren.remove(0);
        if (foldedSize > originalSize) {
          return n;
        }
        arrayNode.detachChildren();
        if (!foldedStringNode.isString()) {
          // If the Node is not a string literal, ensure that
          // it is coerced to a string.
          Node replacement = IR.add(
              IR.string("").srcref(n),
              foldedStringNode);
          foldedStringNode = replacement;
        }
        n.getParent().replaceChild(n, foldedStringNode);
        reportCodeChange();
        return foldedStringNode;
      default:
        // No folding could actually be performed.
        if (arrayFoldedChildren.size() == arrayNode.getChildCount()) {
          return n;
        }
        int kJoinOverhead = "[].join()".length();
        foldedSize += kJoinOverhead;
        foldedSize += (right != null) ? InlineCostEstimator.getCost(right) : 0;
        if (foldedSize > originalSize) {
          return n;
        }
        arrayNode.detachChildren();
        for (Node node : arrayFoldedChildren) {
          arrayNode.addChildToBack(node);
        }
        reportCodeChange();
        break;
    }

    return n;
  }
