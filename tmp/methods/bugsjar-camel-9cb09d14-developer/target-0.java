    private Expression createSimpleExpressionBodyOrHeader(String function, boolean strict) {
        // bodyAs
        String remainder = ifStartsWithReturnRemainder("bodyAs", function);
        if (remainder != null) {
            String type = ObjectHelper.between(remainder, "(", ")");
            remainder = ObjectHelper.after(remainder, ")");
            if (type == null || ObjectHelper.isNotEmpty(remainder)) {
                throw new SimpleParserException("Valid syntax: ${bodyAs(type)} was: " + function, token.getIndex());
            }
            
            type = StringHelper.removeQuotes(type);
            return ExpressionBuilder.bodyExpression(type);
        }
        // mandatoryBodyAs
        remainder = ifStartsWithReturnRemainder("mandatoryBodyAs", function);
        if (remainder != null) {
            String type = ObjectHelper.between(remainder, "(", ")");
            remainder = ObjectHelper.after(remainder, ")");
            if (type == null || ObjectHelper.isNotEmpty(remainder)) {
                throw new SimpleParserException("Valid syntax: ${mandatoryBodyAs(type)} was: " + function, token.getIndex());
            }
            type = StringHelper.removeQuotes(type);
            return ExpressionBuilder.mandatoryBodyExpression(type);
        }

        // body OGNL
        remainder = ifStartsWithReturnRemainder("body", function);
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("in.body", function);
        }
        if (remainder != null) {
            boolean invalid = OgnlHelper.isInvalidValidOgnlExpression(remainder);
            if (invalid) {
                throw new SimpleParserException("Valid syntax: ${body.OGNL} was: " + function, token.getIndex());
            }
            return ExpressionBuilder.bodyOgnlExpression(remainder);
        }

        // headerAs
        remainder = ifStartsWithReturnRemainder("headerAs", function);
        if (remainder != null) {
            String keyAndType = ObjectHelper.between(remainder, "(", ")");
            if (keyAndType == null) {
                throw new SimpleParserException("Valid syntax: ${headerAs(key, type)} was: " + function, token.getIndex());
            }

            String key = ObjectHelper.before(keyAndType, ",");
            String type = ObjectHelper.after(keyAndType, ",");
            remainder = ObjectHelper.after(remainder, ")");
            if (ObjectHelper.isEmpty(key) || ObjectHelper.isEmpty(type) || ObjectHelper.isNotEmpty(remainder)) {
                throw new SimpleParserException("Valid syntax: ${headerAs(key, type)} was: " + function, token.getIndex());
            }
            key = StringHelper.removeQuotes(key);
            type = StringHelper.removeQuotes(type);
            return ExpressionBuilder.headerExpression(key, type);
        }

        // headers function
        if ("in.headers".equals(function) || "headers".equals(function)) {
            return ExpressionBuilder.headersExpression();
        }

        // in header function
        remainder = ifStartsWithReturnRemainder("in.headers", function);
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("in.header", function);
        }
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("headers", function);
        }
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("header", function);
        }
        if (remainder != null) {
            // remove leading character (dot or ?)
            if (remainder.startsWith(".") || remainder.startsWith("?")) {
                remainder = remainder.substring(1);
            }
            // remove starting and ending brackets
            if (remainder.startsWith("[") && remainder.endsWith("]")) {
                remainder = remainder.substring(1, remainder.length() - 1);
            }
            // remove quotes from key
            String key = StringHelper.removeLeadingAndEndingQuotes(remainder);

            // validate syntax
            boolean invalid = OgnlHelper.isInvalidValidOgnlExpression(key);
            if (invalid) {
                throw new SimpleParserException("Valid syntax: ${header.name[key]} was: " + function, token.getIndex());
            }

            if (OgnlHelper.isValidOgnlExpression(key)) {
                // ognl based header
                return ExpressionBuilder.headersOgnlExpression(key);
            } else {
                // regular header
                return ExpressionBuilder.headerExpression(key);
            }
        }

        // out header function
        remainder = ifStartsWithReturnRemainder("out.header.", function);
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("out.headers.", function);
        }
        if (remainder != null) {
            return ExpressionBuilder.outHeaderExpression(remainder);
        }

        return null;
    }
