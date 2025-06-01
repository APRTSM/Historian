    protected void parseCreateDefinition(Marker start, TableEditor table, boolean isAlterStatement) {
        // If the first token is a quoted identifier, then we know it is a column name ...
        Collection<ParsingException> errors = null;
        boolean quoted = isNextTokenQuotedIdentifier();
        Marker defnStart = tokens.mark();
        if (!quoted) {
            // The first token is not quoted so let's check for other expressions ...
            if (tokens.canConsume("CHECK")) {
                // Try to parse the constraints first ...
                consumeExpression(start);
                return;
            }
            if (tokens.canConsume("CONSTRAINT", TokenStream.ANY_VALUE, "PRIMARY", "KEY")
                    || tokens.canConsume("CONSTRAINT", "PRIMARY", "KEY")
                    || tokens.canConsume("PRIMARY", "KEY")
            ) {
                try {
                    if (tokens.canConsume("USING")) {
                        parseIndexType(start);
                    }
                    if (!tokens.matches('(')) {
                        tokens.consume(); // index name
                    }
                    List<String> pkColumnNames = parseIndexColumnNames(start);
                    table.setPrimaryKeyNames(pkColumnNames);
                    parseIndexOptions(start);
                    // MySQL does not allow a primary key to have nullable columns, so let's make sure we model that correctly ...
                    pkColumnNames.forEach(name -> {
                        Column c = table.columnWithName(name);
                        if (c != null && c.isOptional()) {
                            table.addColumn(c.edit().optional(false).create());
                        }
                    });
                    return;
                } catch (ParsingException e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                } catch (MultipleParsingExceptions e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                }
            }
            if (tokens.canConsume("CONSTRAINT", TokenStream.ANY_VALUE, "UNIQUE") || tokens.canConsume("UNIQUE")) {
                tokens.canConsumeAnyOf("KEY", "INDEX");
                try {
                    if (!tokens.matches('(')) {
                        if (!tokens.matches("USING")) {
                            tokens.consume(); // name of unique index ...
                        }
                        if (tokens.matches("USING")) {
                            parseIndexType(start);
                        }
                    }
                    List<String> uniqueKeyColumnNames = parseIndexColumnNames(start);
                    if (table.primaryKeyColumnNames().isEmpty()) {
                        table.setPrimaryKeyNames(uniqueKeyColumnNames); // this may eventually get overwritten by a real PK
                    }
                    parseIndexOptions(start);
                    return;
                } catch (ParsingException e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                } catch (MultipleParsingExceptions e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                }
            }
            if (tokens.canConsume("CONSTRAINT", TokenStream.ANY_VALUE, "FOREIGN", "KEY") || tokens.canConsume("FOREIGN", "KEY")) {
                try {
                    if (!tokens.matches('(')) {
                        tokens.consume(); // name of foreign key
                    }
                    parseIndexColumnNames(start);
                    if (tokens.matches("REFERENCES")) {
                        parseReferenceDefinition(start);
                    }
                    return;
                } catch (ParsingException e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                } catch (MultipleParsingExceptions e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                }
            }
            if (tokens.canConsumeAnyOf("INDEX", "KEY")) {
                try {
                    if (!tokens.matches('(')) {
                        if (!tokens.matches("USING")) {
                            tokens.consume(); // name of unique index ...
                        }
                        if (tokens.matches("USING")) {
                            parseIndexType(start);
                        }
                    }
                    parseIndexColumnNames(start);
                    parseIndexOptions(start);
                    return;
                } catch (ParsingException e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                } catch (MultipleParsingExceptions e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                }
            }
            if (tokens.canConsumeAnyOf("FULLTEXT", "SPATIAL")) {
                try {
                    tokens.canConsumeAnyOf("INDEX", "KEY");
                    if (!tokens.matches('(')) {
                        tokens.consume(); // name of unique index ...
                    }
                    parseIndexColumnNames(start);
                    parseIndexOptions(start);
                    return;
                } catch (ParsingException e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                } catch (MultipleParsingExceptions e) {
                    // Invalid names, so rewind and continue
                    errors = accumulateParsingFailure(e, errors);
                    tokens.rewind(defnStart);
                }
            }
        }

        try {
            // It's either quoted (meaning it's a column definition)
            if (isAlterStatement && !quoted) {
                tokens.canConsume("COLUMN"); // optional for ALTER TABLE
            }

            String columnName = parseColumnName();
            parseCreateColumn(start, table, columnName, null);
        } catch (ParsingException e) {
            if (errors != null) {
                errors = accumulateParsingFailure(e, errors);
                throw new MultipleParsingExceptions(errors);
            }
            throw e;
        } catch (MultipleParsingExceptions e) {
            if (errors != null) {
                errors = accumulateParsingFailure(e, errors);
                throw new MultipleParsingExceptions(errors);
            }
            throw e;
        }
    }
