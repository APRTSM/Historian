  private Node tryFoldArrayJoin(Node n) {
    Node callTarget = n.getFirstChild();

    if (callTarget == null || !callTarget.isGetProp()) {
      return n;
    }

    Node right = callTarget.getNext();
    if (right != null) {
      if (right.getNext() != null || !NodeUtil.isImmutableValue(right)) {
        int index = -1;
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
        return null;
      }
    }
    return res;
  }
