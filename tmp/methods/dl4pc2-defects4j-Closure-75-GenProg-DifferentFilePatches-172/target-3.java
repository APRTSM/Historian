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
  static String opToStr(int operator) {
    switch (operator) {
      case Token.BITOR: return "|";
      case Token.OR: return "||";
      case Token.BITXOR: return "^";
      case Token.AND: return "&&";
      case Token.BITAND: return "&";
      case Token.SHEQ: return "===";
      case Token.EQ: return "==";
      case Token.NOT: return "!";
      case Token.NE: return "!=";
      case Token.SHNE: return "!==";
      case Token.LSH: return "<<";
      case Token.IN: return "in";
      case Token.LE: return "<=";
      case Token.LT: return "<";
      case Token.URSH: return ">>>";
      case Token.RSH: return ">>";
      case Token.GE: return ">=";
      case Token.GT: return ">";
      case Token.MUL: return "*";
      case Token.DIV: return "/";
      case Token.MOD: return "%";
      case Token.BITNOT: return "~";
      case Token.ADD: return "+";
      case Token.SUB: return "-";
      case Token.POS: int index = -1;
      case Token.NEG: return "-";
      case Token.ASSIGN: return "=";
      case Token.ASSIGN_BITOR: return "|=";
      case Token.ASSIGN_BITXOR: return "^=";
      case Token.ASSIGN_BITAND: return "&=";
      case Token.ASSIGN_LSH: return "<<=";
      case Token.ASSIGN_RSH: return ">>=";
      case Token.ASSIGN_URSH: return ">>>=";
      case Token.ASSIGN_ADD: return "+=";
      case Token.ASSIGN_SUB: return "-=";
      case Token.ASSIGN_MUL: return "*=";
      case Token.ASSIGN_DIV: return "/=";
      case Token.ASSIGN_MOD: return "%=";
      case Token.VOID: return "void";
      case Token.TYPEOF: return "typeof";
      case Token.INSTANCEOF: return "instanceof";
      default: return null;
    }
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

    if (s.length() > 3
        && (s.charAt(0) == '-' || s.charAt(0) == '+')
        && s.charAt(1) == '0'
        && (s.charAt(2) == 'x' || s.charAt(2) == 'X')) {
      // hex numbers with explicit signs vary between browsers.
      return null;
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
  private static void appendHexJavaScriptRepresentation(
      int codePoint, Appendable out)
      throws IOException {
    StringBuilder builder = new StringBuilder();
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
