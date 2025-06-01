  private Node parseBasicTypeExpression(JsDocToken token) {
    if (token == JsDocToken.STAR) {
      return newNode(Token.STAR);
    } else if (token == JsDocToken.LB) {
      skipEOLs();
      return parseArrayType(next());
    } else if (token == JsDocToken.LC) {
      skipEOLs();
      return parseRecordType(next());
    } else if (token == JsDocToken.LP) {
      skipEOLs();
      return parseUnionType(next());
    } else if (token == JsDocToken.STRING) {
      String string = stream.getString();
      if ("function".equals(string)) {
        skipEOLs();
        return parseFunctionType(next());
      } else if ("null".equals(string) || "undefined".equals(string)) {
        return newStringNode(string);
      } else {
        return parseTypeName(token);
      }
    }

    restoreLookAhead(token);
    return reportGenericTypeSyntaxWarning();
  }
  public void setPositionInformation(int startLineno, int startCharno,
                                     int endLineno, int endCharno) {
    if (startLineno == endLineno) {
      if (startCharno >= endCharno) {
        throw new IllegalStateException(
            "Recorded bad position information\n" +
            "start-char: " + startCharno + "\n" +
            "end-char: " + endCharno);
      }
    } else {
      if (startLineno > endLineno) {
        throw new IllegalStateException(
            "Recorded bad position information\n" +
            "start-line: " + startLineno + "\n" +
            "end-line: " + endLineno);
      }
    }

    this.startLineno = startLineno;
    this.startCharno = startCharno;
    this.endLineno = endLineno;
    this.endCharno = endCharno;
  }
