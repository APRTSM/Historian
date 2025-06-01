    private Expression parseExpression() throws ParseException {
        if (readIf("@")) {
            return readProperty();
        } else if (readIf("true")) {
            return Expression.Literal.newBoolean(true);
        } else if (readIf("false")) {
            return Expression.Literal.newBoolean(false);
        } else if (currentTokenType == VALUE_NUMBER) {
            Expression.Literal l = Expression.Literal.newNumber(currentToken);
            read();
            return l;
        } else if (currentTokenType == VALUE_STRING) {
            Expression.Literal l = Expression.Literal.newString(currentToken);
            read();
            return l;
        } else if (readIf("-")) {
            if (currentTokenType != VALUE_NUMBER) {
                throw getSyntaxError();
            }
            Expression.Literal l = Expression.Literal.newNumber('-' + currentToken);
            read();
            return l;
        } else if (readIf("+")) {
            if (currentTokenType != VALUE_NUMBER) {
                throw getSyntaxError();
            }
            return parseExpression();
        } else {
            return parsePropertyOrFunction();
        }
    }
