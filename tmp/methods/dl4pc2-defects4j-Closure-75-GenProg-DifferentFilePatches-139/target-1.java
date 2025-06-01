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
