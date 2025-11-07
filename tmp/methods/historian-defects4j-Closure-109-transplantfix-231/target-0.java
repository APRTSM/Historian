  private Node parseTypeName(JsDocToken token) {
    if (token != JsDocToken.STRING) {
      token = next();
if (token == JsDocToken.COMMA ||
          token == JsDocToken.EQUALS ||
          token == JsDocToken.RB ||
          token == JsDocToken.RC ||
          token == JsDocToken.RP ||
          token == JsDocToken.PIPE ||
          token == JsDocToken.GT ||
          token == JsDocToken.EOC) {
restoreLookAhead(token);
return newNode(Token.QMARK);
}

return reportGenericTypeSyntaxWarning();
    }

    String typeName = stream.getString();
    int lineno = stream.getLineno();
    int charno = stream.getCharno();
    while (match(JsDocToken.EOL) &&
        typeName.charAt(typeName.length() - 1) == '.') {
      skipEOLs();
      if (match(JsDocToken.STRING)) {
        next();
        typeName += stream.getString();
      }
    }

    Node typeNameNode = newStringNode(typeName, lineno, charno);

    if (match(JsDocToken.LT)) {
      next();
      skipEOLs();
      Node memberType = parseTypeExpressionList(next());
      if (memberType != null) {
        typeNameNode.addChildToFront(memberType);

        skipEOLs();
        if (!match(JsDocToken.GT)) {
          return reportTypeSyntaxWarning("msg.jsdoc.missing.gt");
        }

        next();
      }
    }
    return typeNameNode;
  }
