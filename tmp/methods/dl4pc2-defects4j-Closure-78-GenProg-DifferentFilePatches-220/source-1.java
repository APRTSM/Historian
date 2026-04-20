  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    currentTraversal.getCompiler().report(error);
  }
  private Node tryFoldAdd(Node node, Node left, Node right) {
    Preconditions.checkArgument(node.getType() == Token.ADD);

    if (NodeUtil.mayBeString(node, true)) {
      if (NodeUtil.isLiteralValue(left, false) &&
          NodeUtil.isLiteralValue(right, false)) {
        // '6' + 7
        return tryFoldAddConstantString(node, left, right);
      } else {
        // a + 7 or 6 + a
        return tryFoldChildAddString(node, left, right);
      }
    } else {
      // Try arithmetic add
      Node result = tryFoldArithmeticOp(node, left, right);
      if (result != node) {
        return result;
      }
      return tryFoldLeftChildOp(node, left, right);
    }
  }
  private Node performArithmeticOp(int opType, Node left, Node right) {
    // Unlike other operations, ADD operands are not always converted
    // to Number.
    if (opType == Token.ADD
        && (NodeUtil.mayBeString(left, false)
            || NodeUtil.mayBeString(right, false))) {
      return null;
    }

    double result;

    // TODO(johnlenz): Handle NaN with unknown value. BIT ops convert NaN
    // to zero so this is a little akward here.

    Double lValObj = NodeUtil.getNumberValue(left);
    if (lValObj == null) {
      return null;
    }
    Double rValObj = NodeUtil.getNumberValue(right);
    if (rValObj == null) {
      return null;
    }

    double lval = lValObj;
    double rval = rValObj;

    switch (opType) {
      case Token.BITAND:
        result = ScriptRuntime.toInt32(lval) & ScriptRuntime.toInt32(rval);
        break;
      case Token.BITOR:
        result = ScriptRuntime.toInt32(lval) | ScriptRuntime.toInt32(rval);
        break;
      case Token.BITXOR:
        result = ScriptRuntime.toInt32(lval) ^ ScriptRuntime.toInt32(rval);
        break;
      case Token.ADD:
        result = lval + rval;
        break;
      case Token.SUB:
        result = lval - rval;
        break;
      case Token.MUL:
        result = lval * rval;
        break;
      case Token.MOD:
        if (rval == 0) {
          error(DiagnosticType.error("JSC_DIVIDE_BY_0_ERROR", "Divide by 0"), right);
          return null;
        }
        result = lval % rval;
        break;
      case Token.DIV:
        if (rval == 0) {
          error(DiagnosticType.error("JSC_DIVIDE_BY_0_ERROR", "Divide by 0"), right);
          return null;
        }
        result = lval / rval;
        break;
      default:
        throw new Error("Unexpected arithmetic operator");
    }

    // TODO(johnlenz): consider removing the result length check.
    // length of the left and right value plus 1 byte for the operator.
    if (String.valueOf(result).length() <=
        String.valueOf(lval).length() + String.valueOf(rval).length() + 1 &&

        // Do not try to fold arithmetic for numbers > 2^53. After that
        // point, fixed-point math starts to break down and become inaccurate.
        Math.abs(result) <= MAX_FOLD_NUMBER) {
      Node newNumber = Node.newNumber(result);
      return newNumber;
    } else if (Double.isNaN(result)) {
      return Node.newString(Token.NAME, "NaN");
    } else if (result == Double.POSITIVE_INFINITY) {
      return Node.newString(Token.NAME, "Infinity");
    } else if (result == Double.NEGATIVE_INFINITY) {
      return new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    }

    return null;
  }
  private Node tryFoldBinaryOperator(Node subtree) {
    Node left = subtree.getFirstChild();

    if (left == null) {
      return subtree;
    }

    Node right = left.getNext();

    if (right == null) {
      return subtree;
    }

    // If we've reached here, node is truly a binary operator.
    switch(subtree.getType()) {
      case Token.GETPROP:
        return tryFoldGetProp(subtree, left, right);

      case Token.GETELEM:
        return tryFoldGetElem(subtree, left, right);

      case Token.INSTANCEOF:
        return tryFoldInstanceof(subtree, left, right);

      case Token.AND:
      case Token.OR:
        return tryFoldAndOr(subtree, left, right);

      case Token.LSH:
      case Token.RSH:
      case Token.URSH:
        return tryFoldShift(subtree, left, right);

      case Token.ASSIGN:
        return tryFoldAssign(subtree, left, right);

      case Token.ADD:
        return tryFoldAdd(subtree, left, right);

      case Token.SUB:
      case Token.DIV:
      case Token.MOD:
        return tryFoldArithmeticOp(subtree, left, right);

      case Token.MUL:
      case Token.BITAND:
      case Token.BITOR:
      case Token.BITXOR:
        Node result = tryFoldArithmeticOp(subtree, left, right);
        if (result != subtree) {
          return result;
        }
        return tryFoldLeftChildOp(subtree, left, right);

      case Token.LT:
      case Token.GT:
      case Token.LE:
      case Token.GE:
      case Token.EQ:
      case Token.NE:
      case Token.SHEQ:
      case Token.SHNE:
        return tryFoldComparison(subtree, left, right);

      default:
        return subtree;
    }
  }
