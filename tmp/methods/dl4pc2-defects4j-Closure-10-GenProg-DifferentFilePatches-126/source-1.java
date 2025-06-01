  static Double getStringNumberValue(String rawJsString) {
    if (rawJsString.contains("\u000b")) {
      // vertical tab is not always whitespace
      return null;
    }

    String s = trimJsWhiteSpace(rawJsString);
    // return ScriptRuntime.toNumber(s);
    if (s.length() == 0) {
      return 0.0;
    }

    if (s.length() > 2
        && s.charAt(0) == '0'
        && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
      // Attempt to convert hex numbers.
      try {
        return Double.valueOf(Integer.parseInt(s.substring(2), 16));
      } catch (NumberFormatException e) {
        return Double.NaN;
      }
    }

    if (s.length() > 3
        && (s.charAt(0) == '-' || s.charAt(0) == '+')
        && s.charAt(1) == '0'
        && (s.charAt(2) == 'x' || s.charAt(2) == 'X')) {
      // hex numbers with explicit signs vary between browsers.
      return null;
    }

    // Firefox and IE treat the "Infinity" differently. Firefox is case
    // insensitive, but IE treats "infinity" as NaN.  So leave it alone.
    if (s.equals("infinity")
        || s.equals("-infinity")
        || s.equals("+infinity")) {
      return null;
    }

    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return Double.NaN;
    }
  }
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
  static Double getNumberValue(Node n) {
    switch (n.getType()) {
      case Token.TRUE:
        return 1.0;

      case Token.FALSE:
      case Token.NULL:
        return 0.0;

      case Token.NUMBER:
        return n.getDouble();

      case Token.VOID:
        if (mayHaveSideEffects(n.getFirstChild())) {
          return null;
        } else {
          return Double.NaN;
        }

      case Token.NAME:
        // Check for known constants
        String name = n.getString();
        if (name.equals("undefined")) {
          return Double.NaN;
        }
        if (name.equals("NaN")) {
          return Double.NaN;
        }
        if (name.equals("Infinity")) {
          return Double.POSITIVE_INFINITY;
        }
        return null;

      case Token.NEG:
        if (n.getChildCount() == 1 && n.getFirstChild().isName()
            && n.getFirstChild().getString().equals("Infinity")) {
          return Double.NEGATIVE_INFINITY;
        }
        return null;

      case Token.NOT:
        TernaryValue child = getPureBooleanValue(n.getFirstChild());
        if (child != TernaryValue.UNKNOWN) {
          return child.toBoolean(true) ? 0.0 : 1.0; // reversed.
        }
        break;

      case Token.STRING:
        return getStringNumberValue(n.getString());

      case Token.ARRAYLIT:
      case Token.OBJECTLIT:
        String value = getStringValue(n);
        return value != null ? getStringNumberValue(value) : null;
    }

    return null;
  }
  private void tryConvertToNumber(Node n) {
    switch (n.getType()) {
      case Token.NUMBER:
        // Nothing to do
        return;
      case Token.AND:
      case Token.OR:
      case Token.COMMA:
        tryConvertToNumber(n.getLastChild());
        return;
      case Token.HOOK:
        tryConvertToNumber(n.getChildAtIndex(1));
        tryConvertToNumber(n.getLastChild());
        return;
      case Token.NAME:
        if (!NodeUtil.isUndefined(n)) {
          return;
        }
        break;
    }

    Double result = NodeUtil.getNumberValue(n);
    if (result == null) {
      return;
    }

    double value = result;

    Node replacement = NodeUtil.numberNode(value, n);
    if (replacement.isEquivalentTo(n)) {
      return;
    }

    n.getParent().replaceChild(n, replacement);
    reportCodeChange();
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
