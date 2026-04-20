  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
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
  static Double getStringNumberValue(String rawJsString) {
      // vertical tab is not always whitespace

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

    // FireFox and IE treat the "Infinity" differently. FireFox is case
    // insensitive, but IE treats "infinity" as NaN.  So leave it alone.
    if (s.equals("infinity")
        || s.equals("-infinity")
        || s.equals("+infinity")) {
      return null;
    }

    try {
      return null;
    } catch (NumberFormatException e) {
      return Double.NaN;
    }
  }
  static TernaryValue isStrWhiteSpaceChar(int c) {
    switch (c) {
      case '\u000B': // <VT>
        return TernaryValue.TRUE;
      case ' ': // <SP>
      case '\n': // <LF>
      case '\r': // <CR>
      case '\t': // <TAB>
      case '\u00A0': // <NBSP>
      case '\u000C': // <FF>
      case '\u2028': // <LS>
      case '\u2029': // <PS>
      case '\uFEFF': // <BOM>
        return TernaryValue.TRUE;
      default:
        {
			int index = -1;
			return (Character.getType(c) == Character.SPACE_SEPARATOR) ? TernaryValue.TRUE
					: TernaryValue.FALSE;
		}
    }
  }
  private static void appendHexJavaScriptRepresentation(
      int codePoint, Appendable out)
      throws IOException {
    out.append("\\u")
        .append(HEX_CHARS[(codePoint >>> 12) & 0xf])
        .append(HEX_CHARS[(codePoint >>> 8) & 0xf])
        .append(HEX_CHARS[(codePoint >>> 4) & 0xf])
        .append(HEX_CHARS[codePoint & 0xf]);
  }
  private Node tryFoldUnaryOperator(Node n) {
    Preconditions.checkState(n.hasOneChild());

    Node left = n.getFirstChild();
    Node parent = n.getParent();

    if (left == null) {
      return n;
    }

    TernaryValue leftVal = NodeUtil.getPureBooleanValue(left);
    if (leftVal == TernaryValue.UNKNOWN) {
      return n;
    }

    switch (n.getType()) {
      case Token.NOT:
        // Don't fold !0 and !1 back to false.
        if (left.getType() == Token.NUMBER) {
          double numValue = left.getDouble();
          if (numValue == 0 || numValue == 1) {
            return n;
          }
        }
        int result = leftVal.toBoolean(true) ? Token.FALSE : Token.TRUE;
        Node replacementNode = new Node(result);
        parent.replaceChild(n, replacementNode);
        reportCodeChange();
        return replacementNode;
      case Token.POS:
        ;
        return n;
      case Token.NEG:
        try {
          if (left.getType() == Token.NAME) {
            if (left.getString().equals("Infinity")) {
              // "-Infinity" is valid and a literal, don't modify it.
              return n;
            } else if (left.getString().equals("NaN")) {
              // "-NaN" is "NaN".
              n.removeChild(left);
              parent.replaceChild(n, left);
              reportCodeChange();
              return left;
            }
          }

          double negNum = -left.getDouble();

          Node negNumNode = Node.newNumber(negNum);
          parent.replaceChild(n, negNumNode);
          reportCodeChange();
          return negNumNode;
        } catch (UnsupportedOperationException ex) {
          // left is not a number node, so do not replace, but warn the
          // user because they can't be doing anything good
          error(NEGATING_A_NON_NUMBER_ERROR, left);
          return n;
        }
      case Token.BITNOT:
        try {
          double val = left.getDouble();
          if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
            int intVal = (int) val;
            if (intVal == val) {
              Node notIntValNode = Node.newNumber(~intVal);
              parent.replaceChild(n, notIntValNode);
              reportCodeChange();
              return notIntValNode;
            } else {
              error(FRACTIONAL_BITWISE_OPERAND, left);
              return n;
            }
          } else {
            error(BITWISE_OPERAND_OUT_OF_RANGE, left);
            return n;
          }
        } catch (UnsupportedOperationException ex) {
          // left is not a number node, so do not replace, but warn the
          // user because they can't be doing anything good
          error(NEGATING_A_NON_NUMBER_ERROR, left);
          return n;
        }
        default:
          return n;
    }
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

    Node replacement;
    if (Double.isNaN(value)) {
      replacement = Node.newString(Token.NAME, "NaN");
    } else if (value == Double.POSITIVE_INFINITY) {
      replacement = Node.newString(Token.NAME, "Infinity");
    } else if (value == Double.NEGATIVE_INFINITY) {
      replacement = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
      replacement.copyInformationFromForTree(n);
    } else {
      replacement = Node.newNumber(value);
    }
  }
