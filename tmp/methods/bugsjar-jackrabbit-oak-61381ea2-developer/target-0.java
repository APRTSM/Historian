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
                    if (supportSQL1 && c == ':') {
                        i++;
                        continue;
                    }
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
            currentToken = statement.substring(start, i);
            currentTokenType = KEYWORD;
            parseIndex = i;
            return;
        case CHAR_SPECIAL_1:
            currentToken = statement.substring(start, i);
            switch (c) {
            case '$':
                currentTokenType = PARAMETER;
                break;
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
                    checkLiterals(false);
                    currentValue = PropertyValues.newLong(number);
                    currentTokenType = VALUE;
                    currentToken = "0";
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
        case CHAR_BRACKETED:
            readString(i, ']');
            currentTokenType = IDENTIFIER;
            currentToken = currentValue.getValue(Type.STRING);
            return;
        case CHAR_STRING:
            readString(i, '\'');
            return;
        case CHAR_QUOTED:
            readString(i, '\"');
            if (supportSQL1) {
                // for SQL-2, this is a literal, as defined in
                // the JCR 2.0 spec, 6.7.34 Literal - UncastLiteral
                // but for compatibility with Jackrabbit 2.x, for
                // SQL-1, this is an identifier, as in ANSI SQL
                // (not in the JCR 1.0 spec)
                // (confusing isn't it?)
                currentTokenType = IDENTIFIER;
                currentToken = currentValue.getValue(Type.STRING);
            }
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
    private StaticOperandImpl parseStaticOperand() throws ParseException {
        if (currentTokenType == PLUS) {
            read();
            if (currentTokenType != VALUE) {
                throw getSyntaxError("number");
            }
            int valueType = currentValue.getType().tag();
            switch (valueType) {
            case PropertyType.LONG:
                currentValue = PropertyValues.newLong(currentValue.getValue(Type.LONG));
                break;
            case PropertyType.DOUBLE:
                currentValue = PropertyValues.newDouble(currentValue.getValue(Type.DOUBLE));
                break;
            case PropertyType.DECIMAL:
                currentValue = PropertyValues.newDecimal(currentValue.getValue(Type.DECIMAL).negate());
                break;
            default:
                throw getSyntaxError("Illegal operation: + " + currentValue);
            }
        } else if (currentTokenType == MINUS) {
            read();
            if (currentTokenType != VALUE) {
                throw getSyntaxError("number");
            }
            int valueType = currentValue.getType().tag();
            switch (valueType) {
            case PropertyType.LONG:
                currentValue = PropertyValues.newLong(-currentValue.getValue(Type.LONG));
                break;
            case PropertyType.DOUBLE:
                currentValue = PropertyValues.newDouble(-currentValue.getValue(Type.DOUBLE));
                break;
            case PropertyType.BOOLEAN:
                currentValue = PropertyValues.newBoolean(!currentValue.getValue(Type.BOOLEAN));
                break;
            case PropertyType.DECIMAL:
                currentValue = PropertyValues.newDecimal(currentValue.getValue(Type.DECIMAL).negate());
                break;
            default:
                throw getSyntaxError("Illegal operation: -" + currentValue);
            }
        }
        if (currentTokenType == VALUE) {
            LiteralImpl literal = getUncastLiteral(currentValue);
            read();
            return literal;
        } else if (currentTokenType == PARAMETER) {
            read();
            String name = readName();
            if (readIf(":")) {
                name = name + ':' + readName();
            }
            BindVariableValueImpl var = bindVariables.get(name);
            if (var == null) {
                var = factory.bindVariable(name);
                bindVariables.put(name, var);
            }
            return var;
        } else if (readIf("TRUE")) {
            LiteralImpl literal = getUncastLiteral(PropertyValues.newBoolean(true));
            return literal;
        } else if (readIf("FALSE")) {
            LiteralImpl literal = getUncastLiteral(PropertyValues.newBoolean(false));
            return literal;
        } else if (readIf("CAST")) {
            read("(");
            StaticOperandImpl op = parseStaticOperand();
            if (!(op instanceof LiteralImpl)) {
                throw getSyntaxError("literal");
            }
            LiteralImpl literal = (LiteralImpl) op;
            PropertyValue value = literal.getLiteralValue();
            read("AS");
            value = parseCastAs(value);
            read(")");
            // CastLiteral
            literal = factory.literal(value);
            return literal;
        } else {
            if (supportSQL1) {
                if (readIf("TIMESTAMP")) {
                    StaticOperandImpl op = parseStaticOperand();
                    if (!(op instanceof LiteralImpl)) {
                        throw getSyntaxError("literal");
                    }
                    LiteralImpl literal = (LiteralImpl) op;
                    PropertyValue value = literal.getLiteralValue();
                    value = PropertyValues.newDate(value.getValue(Type.STRING));
                    literal = factory.literal(value);
                    return literal;
                }
            }
            throw getSyntaxError("static operand");
        }
    }
