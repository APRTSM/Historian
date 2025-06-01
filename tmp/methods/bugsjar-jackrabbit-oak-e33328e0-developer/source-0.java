    private Expression parseFunction(String functionName) throws ParseException {
        if ("jcr:like".equals(functionName)) {
            Condition c = new Condition(parseExpression(), "like", null);
            read(",");
            c.right = parseExpression();
            read(")");
            return c;
        } else if ("jcr:contains".equals(functionName)) {
            Function f = new Function("contains");
            if (readIf(".")) {
                // special case: jcr:contains(., expr)
                f.params.add(new Literal("*"));
            } else {
                f.params.add(parseExpression());
            }
            read(",");
            f.params.add(parseExpression());
            read(")");
            return f;
        } else if ("jcr:score".equals(functionName)) {
            Function f = new Function("score");
            // TODO score: support parameters?
            read(")");
            return f;
        } else if ("xs:dateTime".equals(functionName)) {
            Expression expr = parseExpression();
            Cast c = new Cast(expr, "date");
            read(")");
            return c;
        // } else if ("jcr:deref".equals(functionName)) {
            // TODO support jcr:deref?
        } else {
            throw getSyntaxError("jcr:like | jcr:contains | jcr:score | jcr:deref");
        }
    }
    private void read() throws ParseException {
        currentTokenQuoted = false;
        if (expected != null) {
            expected.clear();
        }
        int[] types = characterTypes;
        int i = parseIndex;
        int type = types[i];
        while (type == 0) {
            type = types[++i];
        }
        int start = i;
        char[] chars = statementChars;
        char c = chars[i++];
        currentToken = "";
        switch (type) {
        case CHAR_NAME:
            while (true) {
                type = types[i];
                if (type != CHAR_NAME && type != CHAR_VALUE) {
                    c = chars[i];
                    break;
                }
                i++;
            }
            currentToken = statement.substring(start, i);
            if (currentToken.isEmpty()) {
                throw getSyntaxError();
            }
            currentTokenType = IDENTIFIER;
            parseIndex = i;
            return;
        case CHAR_SPECIAL_2:
            if (types[i] == CHAR_SPECIAL_2) {
                i++;
            }
            // fall through
        case CHAR_SPECIAL_1:
            currentToken = statement.substring(start, i);
            switch (c) {
            case '+':
                currentTokenType = PLUS;
                break;
            case '-':
                currentTokenType = MINUS;
                break;
            case '(':
                currentTokenType = OPEN;
                break;
            case ')':
                currentTokenType = CLOSE;
                break;
            default:
                currentTokenType = KEYWORD;
            }
            parseIndex = i;
            return;
        case CHAR_VALUE:
            long number = c - '0';
            while (true) {
                c = chars[i];
                if (c < '0' || c > '9') {
                    if (c == '.') {
                        readDecimal(start, i);
                        break;
                    }
                    if (c == 'E' || c == 'e') {
                        readDecimal(start, i);
                        break;
                    }
                    currentTokenType = VALUE_NUMBER;
                    currentToken = String.valueOf(number);
                    parseIndex = i;
                    break;
                }
                number = number * 10 + (c - '0');
                if (number > Integer.MAX_VALUE) {
                    readDecimal(start, i);
                    break;
                }
                i++;
            }
            return;
        case CHAR_DECIMAL:
            if (types[i] != CHAR_VALUE) {
                currentTokenType = KEYWORD;
                currentToken = ".";
                parseIndex = i;
                return;
            }
            readDecimal(i - 1, i);
            return;
        case CHAR_STRING:
            readString(i, '\'');
            return;
        case CHAR_END:
            currentToken = "";
            currentTokenType = END;
            parseIndex = i;
            return;
        default:
            throw getSyntaxError();
        }
    }
