  void tryMinimizeExits(Node n, int exitType, String labelName) {

    // Just an 'exit'.
    if (matchingExitNode(n, exitType, labelName)) {
      NodeUtil.removeChild(n.getParent(), n);
      compiler.reportCodeChange();
      return;
    }

    // Just an 'if'.
    if (n.isIf()) {
      Node ifBlock = n.getFirstChild().getNext();
      tryMinimizeExits(ifBlock, exitType, labelName);
      Node elseBlock = ifBlock.getNext();
      if (elseBlock != null) {
        tryMinimizeExits(elseBlock, exitType, labelName);
      }
      return;
    }

    // Just a 'try/catch/finally'.
    if (n.isTry()) {
      Node tryBlock = n.getFirstChild();
      tryMinimizeExits(tryBlock, exitType, labelName);
      Node allCatchNodes = NodeUtil.getCatchBlock(n);
      if (NodeUtil.hasCatchHandler(allCatchNodes)) {
        Preconditions.checkState(allCatchNodes.hasOneChild());
        Node catchNode = allCatchNodes.getFirstChild();
        Node catchCodeBlock = catchNode.getLastChild();
        tryMinimizeExits(catchCodeBlock, exitType, labelName);
      }
    }

    // Just a 'label'.
    if (n.isLabel()) {
      Node labelBlock = n.getLastChild();
      tryMinimizeExits(labelBlock, exitType, labelName);
    }

    // TODO(johnlenz): The last case of SWITCH statement?

    // The rest assumes a block with at least one child, bail on anything else.
    if (!n.isBlock() || n.getLastChild() == null) {
      return;
    }

    // Multiple if-exits can be converted in a single pass.
    // Convert "if (blah) break;  if (blah2) break; other_stmt;" to
    // become "if (blah); else { if (blah2); else { other_stmt; } }"
    // which will get converted to "if (!blah && !blah2) { other_stmt; }".
    for (Node c : n.children()) {

      // An 'if' block to process below.
      if (c.isIf()) {
        Node ifTree = c;
        Node trueBlock, falseBlock;

        // First, the true condition block.
        trueBlock = ifTree.getFirstChild().getNext();
        falseBlock = trueBlock.getNext();
        tryMinimizeIfBlockExits(trueBlock, falseBlock,
            ifTree, exitType, labelName);

        // Now the else block.
        // The if blocks may have changed, get them again.
        trueBlock = ifTree.getFirstChild().getNext();
        falseBlock = trueBlock.getNext();
        if (falseBlock != null) {
          tryMinimizeIfBlockExits(falseBlock, trueBlock,
              ifTree, exitType, labelName);
        }
      }

      if (c == n.getLastChild()) {
        break;
      }
    }

    // Now try to minimize the exits of the last child, if it is removed
    // look at what has become the last child.
    for (Node c = n.getLastChild(); c != null; c = n.getLastChild()) {
      tryMinimizeExits(c, exitType, labelName);
      // If the node is still the last child, we are done.
      if (c == n.getLastChild()) {
        break;
      }
    }
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
      }
    }
    return res;
  }
  private void toString(
      StringBuilder sb,
      boolean printSource,
      boolean printAnnotations,
      boolean printType) {
    sb.append(Token.name(type));
    if (this instanceof StringNode) {
      sb.append(' ');
      sb.append(getString());
    } else if (type == Token.FUNCTION) {
      sb.append(' ');
      // In the case of JsDoc trees, the first child is often not a string
      // which causes exceptions to be thrown when calling toString or
      // toStringTree.
      if (first == null || first.getType() != Token.NAME) {
        sb.append("<invalid>");
      } else {
        int[] keys = getSortedPropTypes();
      }
    } else if (type == Token.NUMBER) {
      sb.append(' ');
      sb.append(getDouble());
    }
    if (printSource) {
      int lineno = getLineno();
      if (lineno != -1) {
        sb.append(' ');
        sb.append(lineno);
      }
    }

    if (printAnnotations) {
      int[] keys = getSortedPropTypes();
      for (int i = 0; i < keys.length; i++) {
        int type = keys[i];
        PropListItem x = lookupProperty(type);
        sb.append(" [");
        sb.append(propToString(type));
        sb.append(": ");
        String value;
        switch (type) {
          default:
            value = x.toString();
            break;
        }
        sb.append(value);
        sb.append(']');
      }
    }

    if (printType) {
      if (jsType != null) {
        String jsTypeString = jsType.toString();
        if (jsTypeString != null) {
          sb.append(" : ");
          sb.append(jsTypeString);
        }
      }
    }
  }
