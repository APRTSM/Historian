    private Expression createSimpleExpression(String function, boolean strict) {
        // return the function directly if we can create function without analyzing the prefix
        Expression answer = createSimpleExpressionDirectly(function);
        if (answer != null) {
            return answer;
        }

        // body and headers first
        answer = createSimpleExpressionBodyOrHeader(function, strict);
        if (answer != null) {
            return answer;
        }

        // camelContext OGNL
        String remainder = ifStartsWithReturnRemainder("camelContext", function);
        if (remainder != null) {
            boolean invalid = OgnlHelper.isInvalidValidOgnlExpression(remainder);
            if (invalid) {
                throw new SimpleParserException("Valid syntax: ${camelContext.OGNL} was: " + function, token.getIndex());
            }
            return ExpressionBuilder.camelContextOgnlExpression(remainder);
        }

        // Exception OGNL
        remainder = ifStartsWithReturnRemainder("exception", function);
        if (remainder != null) {
            boolean invalid = OgnlHelper.isInvalidValidOgnlExpression(remainder);
            if (invalid) {
                throw new SimpleParserException("Valid syntax: ${exception.OGNL} was: " + function, token.getIndex());
            }
            return ExpressionBuilder.exchangeExceptionOgnlExpression(remainder);
        }

        // property
        remainder = ifStartsWithReturnRemainder("property", function);
        if (remainder == null) {
            remainder = ifStartsWithReturnRemainder("exchangeProperty", function);
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

            // validate syntax
            boolean invalid = OgnlHelper.isInvalidValidOgnlExpression(remainder);
            if (invalid) {
                throw new SimpleParserException("Valid syntax: ${exchangeProperty.OGNL} was: " + function, token.getIndex());
            }

            if (OgnlHelper.isValidOgnlExpression(remainder)) {
                // ognl based property
                return ExpressionBuilder.propertyOgnlExpression(remainder);
            } else {
                // regular property
                return ExpressionBuilder.exchangePropertyExpression(remainder);
            }
        }

        // system property
        remainder = ifStartsWithReturnRemainder("sys.", function);
        if (remainder != null) {
            return ExpressionBuilder.systemPropertyExpression(remainder);
        }
        remainder = ifStartsWithReturnRemainder("sysenv.", function);
        if (remainder != null) {
            return ExpressionBuilder.systemEnvironmentExpression(remainder);
        }

        // exchange OGNL
        remainder = ifStartsWithReturnRemainder("exchange", function);
        if (remainder != null) {
            boolean invalid = OgnlHelper.isInvalidValidOgnlExpression(remainder);
            if (invalid) {
                throw new SimpleParserException("Valid syntax: ${exchange.OGNL} was: " + function, token.getIndex());
            }
            return ExpressionBuilder.exchangeOgnlExpression(remainder);
        }

        // file: prefix
        remainder = ifStartsWithReturnRemainder("file:", function);
        if (remainder != null) {
            Expression fileExpression = createSimpleFileExpression(remainder, strict);
            if (fileExpression != null) {
                return fileExpression;
            }
        }

        // date: prefix
        remainder = ifStartsWithReturnRemainder("date:", function);
        if (remainder != null) {
            String[] parts = remainder.split(":");
            if (parts.length < 2) {
                throw new SimpleParserException("Valid syntax: ${date:command:pattern} was: " + function, token.getIndex());
            }
            String command = ObjectHelper.before(remainder, ":");
            String pattern = ObjectHelper.after(remainder, ":");
            return ExpressionBuilder.dateExpression(command, pattern);
        }

        // bean: prefix
        remainder = ifStartsWithReturnRemainder("bean:", function);
        if (remainder != null) {
            return ExpressionBuilder.beanExpression(remainder);
        }

        // properties: prefix
        remainder = ifStartsWithReturnRemainder("properties:", function);
        if (remainder != null) {
            String[] parts = remainder.split(":");
            if (parts.length > 2) {
                throw new SimpleParserException("Valid syntax: ${properties:key[:default]} was: " + function, token.getIndex());
            }
            return ExpressionBuilder.propertiesComponentExpression(remainder, null);
        }

        // properties-location: prefix
        remainder = ifStartsWithReturnRemainder("properties-location:", function);
        if (remainder != null) {
            String[] parts = remainder.split(":");
            if (parts.length > 3) {
                throw new SimpleParserException("Valid syntax: ${properties-location:location:key[:default]} was: " + function, token.getIndex());
            }

            String locations = null;
            String key = remainder;
            if (parts.length >= 2) {
                locations = ObjectHelper.before(remainder, ":");
                key = ObjectHelper.after(remainder, ":");
            }
            return ExpressionBuilder.propertiesComponentExpression(key, locations);
        }

        // ref: prefix
        remainder = ifStartsWithReturnRemainder("ref:", function);
        if (remainder != null) {
            return ExpressionBuilder.refExpression(remainder);
        }

        // const: prefix
        remainder = ifStartsWithReturnRemainder("type:", function);
        if (remainder != null) {
            Expression exp = ExpressionBuilder.typeExpression(remainder);
            // we want to cache this expression so we wont re-evaluate it as the type/constant wont change
            return ExpressionBuilder.cacheExpression(exp);
        }

        // random
        remainder = ifStartsWithReturnRemainder("random", function);
        if (remainder != null) {
            String values = ObjectHelper.between(remainder, "(", ")");
            if (values == null || ObjectHelper.isEmpty(values)) {
                throw new SimpleParserException("Valid syntax: ${random(min,max)} or ${random(max)} was: " + function, token.getIndex());
            }
            if (values.contains(",")) {
                String[] tokens = values.split(",", -1);
                if (tokens.length > 2) {
                    throw new SimpleParserException("Valid syntax: ${random(min,max)} or ${random(max)} was: " + function, token.getIndex());
                }
                int min = Integer.parseInt(tokens[0].trim());
                int max = Integer.parseInt(tokens[1].trim());
                return ExpressionBuilder.randomExpression(min, max);
            } else {
                int max = Integer.parseInt(values.trim());
                return ExpressionBuilder.randomExpression(max);
            }
        }

        // collate function
        remainder = ifStartsWithReturnRemainder("collate", function);
        if (remainder != null) {
            String values = ObjectHelper.between(remainder, "(", ")");
            if (values == null || ObjectHelper.isEmpty(values)) {
                throw new SimpleParserException("Valid syntax: ${collate(group)} was: " + function, token.getIndex());
            }
            String exp = "${body}";
            int num = Integer.parseInt(values.trim());
            return ExpressionBuilder.collateExpression(exp, num);
        }

        if (strict) {
            throw new SimpleParserException("Unknown function: " + function, token.getIndex());
        } else {
            return null;
        }
    }
    private Expression createSimpleFileExpression(String remainder, boolean strict) {
        if (ObjectHelper.equal(remainder, "name")) {
            return ExpressionBuilder.fileNameExpression();
        } else if (ObjectHelper.equal(remainder, "name.noext")) {
            return ExpressionBuilder.fileNameNoExtensionExpression();
        } else if (ObjectHelper.equal(remainder, "name.noext.single")) {
            return ExpressionBuilder.fileNameNoExtensionSingleExpression();
        } else if (ObjectHelper.equal(remainder, "name.ext") || ObjectHelper.equal(remainder, "ext")) {
            return ExpressionBuilder.fileExtensionExpression();
        } else if (ObjectHelper.equal(remainder, "name.ext.single")) {
            return ExpressionBuilder.fileExtensionSingleExpression();
        } else if (ObjectHelper.equal(remainder, "onlyname")) {
            return ExpressionBuilder.fileOnlyNameExpression();
        } else if (ObjectHelper.equal(remainder, "onlyname.noext")) {
            return ExpressionBuilder.fileOnlyNameNoExtensionExpression();
        } else if (ObjectHelper.equal(remainder, "onlyname.noext.single")) {
            return ExpressionBuilder.fileOnlyNameNoExtensionSingleExpression();
        } else if (ObjectHelper.equal(remainder, "parent")) {
            return ExpressionBuilder.fileParentExpression();
        } else if (ObjectHelper.equal(remainder, "path")) {
            return ExpressionBuilder.filePathExpression();
        } else if (ObjectHelper.equal(remainder, "absolute")) {
            return ExpressionBuilder.fileAbsoluteExpression();
        } else if (ObjectHelper.equal(remainder, "absolute.path")) {
            return ExpressionBuilder.fileAbsolutePathExpression();
        } else if (ObjectHelper.equal(remainder, "length") || ObjectHelper.equal(remainder, "size")) {
            return ExpressionBuilder.fileSizeExpression();
        } else if (ObjectHelper.equal(remainder, "modified")) {
            return ExpressionBuilder.fileLastModifiedExpression();
        }
        if (strict) {
            throw new SimpleParserException("Unknown file language syntax: " + remainder, token.getIndex());
        }
        return null;
    }
