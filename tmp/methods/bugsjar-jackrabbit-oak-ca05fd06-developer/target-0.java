    private Statement convertToStatement(String query) throws ParseException {
        
        query = query.trim();
        
        Statement statement = new Statement();

        if (query.startsWith("explain ")) {
            query = query.substring("explain".length()).trim();
            statement.setExplain(true);
        }
        if (query.startsWith("measure")) {
            query = query.substring("measure".length()).trim();
            statement.setMeasure(true);
        }
        
        if (query.isEmpty()) {
            // special case, will always result in an empty result
            query = "//jcr:root";
        }
        
        statement.setOriginalQuery(query);
        
        initialize(query);
        
        expected = new ArrayList<String>();
        read();
        
        if (currentTokenType == END) {
            throw getSyntaxError("the query may not be empty");
        }

        currentSelector.name = "a";

        String pathPattern = "";
        boolean startOfQuery = true;

        while (true) {
            
            // if true, path or nodeType conditions are not allowed
            boolean shortcut = false;
            boolean slash = readIf("/");
            
            if (!slash) {
                if (startOfQuery) {
                    // the query doesn't start with "/"
                    currentSelector.path = "/";
                    pathPattern = "/";
                    currentSelector.isChild = true;
                } else {
                    break;
                }
            } else if (readIf("jcr:root")) {
                // "/jcr:root" may only appear at the beginning
                if (!pathPattern.isEmpty()) {
                    throw getSyntaxError("jcr:root needs to be at the beginning");
                }
                if (readIf("/")) {
                    // "/jcr:root/"
                    currentSelector.path = "/";
                    pathPattern = "/";
                    if (readIf("/")) {
                        // "/jcr:root//"
                        pathPattern = "//";
                        currentSelector.isDescendant = true;
                    } else {
                        currentSelector.isChild = true;
                    }
                } else {
                    // for example "/jcr:root[condition]"
                    pathPattern = "/%";
                    currentSelector.path = "/";
                    shortcut = true;
                }
            } else if (readIf("/")) {
                // "//" was read
                pathPattern += "%";
                if (currentSelector.isDescendant) {
                    // the query started with "//", and now "//" was read
                    nextSelector(true);
                }
                currentSelector.isDescendant = true;
            } else {
                // the token "/" was read
                pathPattern += "/";
                if (startOfQuery) {
                    currentSelector.path = "/";
                } else {
                    if (currentSelector.isDescendant) {
                        // the query started with "//", and now "/" was read
                        nextSelector(true);
                    }
                    currentSelector.isChild = true;
                }
            }
            int startParseIndex = parseIndex;
            if (shortcut) {
                // "*" and so on are not allowed now
            } else if (readIf("*")) {
                // "...*"
                pathPattern += "%";
                if (!currentSelector.isDescendant) {
                    if (selectors.size() == 0 && currentSelector.path.equals("")) {
                        // the query /* is special
                        currentSelector.path = "/";
                    }
                }
            } else if (currentTokenType == IDENTIFIER) {
                // probably a path restriction
                // String name = readPathSegment();
                String identifier = readIdentifier();
                if (readIf("(")) {
                    if ("text".equals(identifier)) {
                        // "...text()"
                        currentSelector.isChild = false;
                        pathPattern += "jcr:xmltext";
                        read(")");
                        if (currentSelector.isDescendant) {
                            currentSelector.nodeName = "jcr:xmltext";
                        } else {
                            currentSelector.path = PathUtils.concat(currentSelector.path, "jcr:xmltext");
                        }
                    } else if ("element".equals(identifier)) {
                        // "...element(..."
                        if (readIf(")")) {
                            // any
                            pathPattern += "%";
                        } else {
                            if (readIf("*")) {
                                // any
                                pathPattern += "%";
                            } else {
                                String name = readPathSegment();
                                pathPattern += name;
                                appendNodeName(name);
                            }
                            if (readIf(",")) {
                                currentSelector.nodeType = readIdentifier();
                            }
                            read(")");
                        }
                    } else if ("rep:excerpt".equals(identifier)) {
                        readOpenDotClose(false);
                        rewindSelector();
                        Expression.Property p = new Expression.Property(currentSelector, "rep:excerpt", false);
                        statement.addSelectColumn(p);
                    } else {
                        throw getSyntaxError();
                    }
                } else {
                    String name = ISO9075.decode(identifier);
                    pathPattern += name;
                    appendNodeName(name);
                }
            } else if (readIf("@")) {
                rewindSelector();
                Expression.Property p = readProperty();
                statement.addSelectColumn(p);
            } else if (readIf("(")) {
                rewindSelector();
                do {
                    if (readIf("@")) {
                        Expression.Property p = readProperty();
                        statement.addSelectColumn(p);
                    } else if (readIf("rep:excerpt")) {
                        readOpenDotClose(true);
                        Expression.Property p = new Expression.Property(currentSelector, "rep:excerpt", false);
                        statement.addSelectColumn(p);
                    } else if (readIf("rep:spellcheck")) {
                        // only rep:spellcheck() is currently supported
                        read("(");
                        read(")");                        
                        Expression.Property p = new Expression.Property(currentSelector, "rep:spellcheck()", false);
                        statement.addSelectColumn(p);
                    } else if (readIf("rep:suggest")) {
                        readOpenDotClose(true);
                        Expression.Property p = new Expression.Property(currentSelector, "rep:suggest()", false);
                        statement.addSelectColumn(p);
                    }
                } while (readIf("|"));
                if (!readIf(")")) {
                    return convertToUnion(query, statement, startParseIndex - 1);
                }
            } else if (readIf(".")) {
                // just "." this is simply ignored, so that
                // "a/./b" is the same as "a/b"
                if (readIf(".")) {
                    // ".." means "the parent of the node"
                    // handle like a regular path restriction
                    String name = "..";
                    pathPattern += name;
                    if (!currentSelector.isChild) {
                        currentSelector.nodeName = name;
                    } else {
                        if (currentSelector.isChild) {
                            currentSelector.isChild = false;
                            currentSelector.isParent = true;
                        }
                    }
                } else {
                    if (selectors.size() > 0) {
                        currentSelector = selectors.remove(selectors.size() - 1);
                        currentSelector.condition = null;
                        currentSelector.joinCondition = null;
                    }
                }
            } else {
                throw getSyntaxError();
            }
            if (readIf("[")) {
                Expression c = parseConstraint();
                currentSelector.condition = Expression.and(currentSelector.condition, c);
                read("]");
            }
            startOfQuery = false;
            nextSelector(false);
        }
        if (selectors.size() == 0) {
            nextSelector(true);
        }
        // the current selector wasn't used so far
        // go back to the last one
        currentSelector = selectors.get(selectors.size() - 1);
        if (selectors.size() == 1) {
            currentSelector.onlySelector = true;
        }
        if (readIf("order")) {
            read("by");
            do {
                Order order = new Order();
                order.expr = parseExpression();
                if (readIf("descending")) {
                    order.descending = true;
                } else {
                    readIf("ascending");
                }
                statement.addOrderBy(order);
            } while (readIf(","));
        }
        if (!currentToken.isEmpty()) {
            throw getSyntaxError("<end>");
        }
        statement.setColumnSelector(currentSelector);
        statement.setSelectors(selectors);
        
        Expression where = null;
        for (Selector s : selectors) {
            where = Expression.and(where, s.condition);
        }
        statement.setWhere(where);
        return statement;
    }
    private void readOpenDotClose(boolean readOpenBracket) throws ParseException {
        if (readOpenBracket) {
            read("(");
        }
        readIf(".");
        read(")");
    }
