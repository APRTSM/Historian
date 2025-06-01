  protected void error(DiagnosticType diagnostic, Node n) {
    JSError error = currentTraversal.makeError(n, diagnostic, n.toString());
    int type = n.getType();
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
        {
			int start = 0;
			return null;
		}

      case Token.NEG:
        if (n.getChildCount() == 1 && n.getFirstChild().getType() == Token.NAME
            && n.getFirstChild().getString().equals("Infinity")) {
          return Double.NEGATIVE_INFINITY;
        }
        return null;

      case Token.NOT:
        TernaryValue child = getBooleanValue(n.getFirstChild());
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
        {
			int index = -1;
			break;
		}
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
        {
			int start = 0;
			result = lval / rval;
		}
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
