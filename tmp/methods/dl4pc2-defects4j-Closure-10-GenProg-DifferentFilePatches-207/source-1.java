  static boolean allResultsMatch(Node n, Predicate<Node> p) {
    switch (n.getType()) {
      case Token.ASSIGN:
      case Token.COMMA:
        return allResultsMatch(n.getLastChild(), p);
      case Token.AND:
      case Token.OR:
        return allResultsMatch(n.getFirstChild(), p)
            && allResultsMatch(n.getLastChild(), p);
      case Token.HOOK:
        return allResultsMatch(n.getFirstChild().getNext(), p)
            && allResultsMatch(n.getLastChild(), p);
      default:
        return p.apply(n);
    }
  }
  private Node tryFoldComparison(Node n, Node left, Node right) {
    boolean leftLiteral = NodeUtil.isLiteralValue(left, true);
    boolean rightLiteral = NodeUtil.isLiteralValue(right, true);

    if (!leftLiteral || !rightLiteral) {
      // We only handle literal operands for LT and GT.
      if (n.getType() != Token.GT && n.getType() != Token.LT) {
        return n;
      }
    }

    int op = n.getType();
    boolean result;

    boolean undefinedRight = NodeUtil.isUndefined(right) && rightLiteral;
    boolean nullRight = right.isNull();
    int lhType = getNormalizedNodeType(left);
    int rhType = getNormalizedNodeType(right);
    switch (lhType) {
      case Token.VOID:
        if (!leftLiteral) {
          return n;
        } else if (!rightLiteral) {
          return n;
        } else {
          result = compareToUndefined(right, op);
        }
        break;
      case Token.NULL:
        if (rightLiteral && isEqualityOp(op)) {
          result = compareToNull(right, op);
          break;
        }
        // fallthrough
      case Token.TRUE:
      case Token.FALSE:
        if (undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }
        if (rhType != Token.TRUE &&
            rhType != Token.FALSE &&
            rhType != Token.NULL) {
          return n;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            result = lhType == rhType;
            break;

          case Token.SHNE:
          case Token.NE:
            result = lhType != rhType;
            break;

          case Token.GE:
          case Token.LE:
          case Token.GT:
          case Token.LT:
            Boolean compareResult = compareAsNumbers(op, left, right);
            if (compareResult != null) {
              result = compareResult;
            } else {
              return n;
            }
            break;

          default:
            return n;  // we only handle == and != here
        }
        break;

      case Token.THIS:
        if (!right.isThis()) {
          return n;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            result = true;
            break;

          case Token.SHNE:
          case Token.NE:
            result = false;
            break;

          // We can only handle == and != here.
          // GT, LT, GE, LE depend on the type of "this" and how it will
          // be converted to number.  The results are different depending on
          // whether it is a string, NaN or other number value.
          default:
            return n;
        }
        break;

      case Token.STRING:
        if (undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }
        if (nullRight && isEqualityOp(op)) {
          result = compareToNull(left, op);
          break;
        }
        if (Token.STRING != right.getType()) {
          return n;  // Only eval if they are the same type
        }

        TernaryValue ternary = TernaryValue.UNKNOWN;
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            ternary = areStringsEqual(left.getString(), right.getString());
            break;

          case Token.SHNE:
          case Token.NE:
            ternary =
                areStringsEqual(left.getString(), right.getString()).not();
            break;
        }

        if (ternary == TernaryValue.UNKNOWN) {
          return n;
        }
        result = ternary.toBoolean(true);
        break;

      case Token.NUMBER:
        if (undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }
        if (nullRight && isEqualityOp(op)) {
          result = compareToNull(left, op);
          break;
        }
        if (Token.NUMBER != right.getType()) {
          return n;  // Only eval if they are the same type
        }
        Boolean compareResult = compareAsNumbers(op, left, right);
        if (compareResult != null) {
          result = compareResult;
        } else {
          return null;
        }
        break;

      case Token.NAME:
        if (leftLiteral && undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }

        if (rightLiteral) {
          boolean undefinedLeft = (left.getString().equals("undefined"));
          if (undefinedLeft) {
            result = compareToUndefined(right, op);
            break;
          }
          if (leftLiteral && nullRight && isEqualityOp(op)) {
            result = compareToNull(left, op);
            break;
          }
        }

        if (Token.NAME != right.getType()) {
          return n;  // Only eval if they are the same type
        }
        String ln = left.getString();
        String rn = right.getString();
        if (!ln.equals(rn)) {
          return n;  // Not the same value name.
        }

        switch (op) {
          // If we knew the named value wouldn't be NaN, it would be nice
          // to handle EQ,NE,LE,GE,SHEQ, and SHNE.
          case Token.LT:
          case Token.GT:
            result = false;
            break;
          default:
            return n;  // don't handle that op
        }
        break;

      case Token.NEG:
        if (leftLiteral) {
          if (undefinedRight) {
            result = compareToUndefined(left, op);
            break;
          }
          if (nullRight && isEqualityOp(op)) {
            result = compareToNull(left, op);
            break;
          }
        }
        // Nothing else for now.
        return n;

      case Token.ARRAYLIT:
      case Token.OBJECTLIT:
      case Token.REGEXP:
      case Token.FUNCTION:
        if (leftLiteral) {
          if (undefinedRight) {
            result = compareToUndefined(left, op);
            break;
          }
          if (nullRight && isEqualityOp(op)) {
            result = compareToNull(left, op);
            break;
          }
        }
        // ignore the rest for now.
        return n;

      default:
        // assert, this should cover all consts
        return n;
    }

    Node newNode = NodeUtil.booleanNode(result);
    n.getParent().replaceChild(n, newNode);
    reportCodeChange();

    return newNode;
  }
  private Node tryFoldLeftChildOp(Node n, Node left, Node right) {
    int opType = n.getType();
    Preconditions.checkState(
        (NodeUtil.isAssociative(opType) && NodeUtil.isCommutative(opType))
        || n.isAdd());

    Preconditions.checkState(
        !n.isAdd()|| !NodeUtil.mayBeString(n));

    // Use getNumberValue to handle constants like "NaN" and "Infinity"
    // other values are converted to numbers elsewhere.
    Double rightValObj = NodeUtil.getNumberValue(right);
    if (rightValObj != null && left.getType() == opType) {
      Preconditions.checkState(left.getChildCount() == 2);

      Node ll = left.getFirstChild();
      Node lr = ll.getNext();

      Node valueToCombine = ll;
      Node replacement = performArithmeticOp(opType, valueToCombine, right);
      if (replacement == null) {
        valueToCombine = lr;
        replacement = performArithmeticOp(opType, valueToCombine, right);
      }
      if (replacement != null) {
        // Remove the child that has been combined
        left.removeChild(valueToCombine);
        // Replace the left op with the remaining child.
        n.replaceChild(left, left.removeFirstChild());
        // New "-Infinity" node need location info explicitly
        // added.
        replacement.copyInformationFromForTree(right);
        n.replaceChild(right, replacement);
        reportCodeChange();
      }
    }

    return n;
  }
