    protected void parseColumnDefinition(Marker start, String columnName, TokenStream tokens, TableEditor table, ColumnEditor column,
                                         AtomicBoolean isPrimaryKey) {
        // Parse the data type, which must be at this location ...
        List<ParsingException> errors = new ArrayList<>();
        Marker dataTypeStart = tokens.mark();
        DataType dataType = dataTypeParser.parse(tokens, errors::addAll);
        if (dataType == null) {
            String dataTypeName = parseDomainName(start);
            if (dataTypeName != null) dataType = DataType.userDefinedType(dataTypeName);
        }
        if (dataType == null) {
            // No data type was found
            parsingFailed(dataTypeStart.position(), errors, "Unable to read the data type");
            return;
        }
        column.jdbcType(dataType.jdbcType());
        column.type(dataType.name(), dataType.expression());
        if ("ENUM".equals(dataType.name())) {
            column.length(1);
        } else if ("SET".equals(dataType.name())) {
            List<String> options = parseSetAndEnumOptions(dataType.expression());
            // After DBZ-132, it will always be comma seperated
            column.length(Math.max(0, options.size() * 2 - 1)); // number of options + number of commas
        } else {
            if (dataType.length() > -1) column.length((int) dataType.length());
            if (dataType.scale() > -1) column.scale(dataType.scale());
        }

        if (Types.NCHAR == dataType.jdbcType() || Types.NVARCHAR == dataType.jdbcType()) {
            // NCHAR and NVARCHAR columns always uses utf8 as charset
            column.charsetName("utf8");
        }

        if (Types.DECIMAL == dataType.jdbcType()) {
            if (dataType.length() == -1) {
                column.length(10);
            }
            if (dataType.scale() == -1) {
                column.scale(0);
            }
        }

        if (tokens.canConsume("CHARSET") || tokens.canConsume("CHARACTER", "SET")) {
            String charsetName = tokens.consume();
            if (!"DEFAULT".equalsIgnoreCase(charsetName)) {
                // Only record it if not inheriting the character set from the table
                column.charsetName(charsetName);
            }
        }
        if (tokens.canConsume("COLLATE")) {
            tokens.consume(); // name of collation
        }

        if (tokens.canConsume("AS") || tokens.canConsume("GENERATED", "ALWAYS", "AS")) {
            consumeExpression(start);
            tokens.canConsumeAnyOf("VIRTUAL", "STORED");
            if (tokens.canConsume("UNIQUE")) {
                tokens.canConsume("KEY");
            }
            if (tokens.canConsume("COMMENT")) {
                consumeQuotedString();
            }
            tokens.canConsume("NOT", "NULL");
            tokens.canConsume("NULL");
            tokens.canConsume("PRIMARY", "KEY");
            tokens.canConsume("KEY");
        } else {
            while (tokens.matchesAnyOf("NOT", "NULL", "DEFAULT", "AUTO_INCREMENT", "UNIQUE", "PRIMARY", "KEY", "COMMENT",
                                       "REFERENCES", "COLUMN_FORMAT", "ON", "COLLATE")) {
                // Nullability ...
                if (tokens.canConsume("NOT", "NULL")) {
                    column.optional(false);
                } else if (tokens.canConsume("NULL")) {
                    column.optional(true);
                }
                // Default value ...
                if (tokens.matches("DEFAULT")) {
                    parseDefaultClause(start);
                }
                if (tokens.matches("ON", "UPDATE") || tokens.matches("ON", "DELETE")) {
                    parseOnUpdateOrDelete(tokens.mark());
                    column.autoIncremented(true);
                }
                // Other options ...
                if (tokens.canConsume("AUTO_INCREMENT")) {
                    column.autoIncremented(true);
                    column.generated(true);
                }
                if (tokens.canConsume("UNIQUE", "KEY") || tokens.canConsume("UNIQUE")) {
                    if (table.primaryKeyColumnNames().isEmpty() && !column.isOptional()) {
                        // The table has no primary key (yet) but this is a non-null column and therefore will have all unique
                        // values (MySQL allows UNIQUE indexes with some nullable columns, but in that case allows duplicate
                        // rows),
                        // so go ahead and set it to this column as it's a unique key
                        isPrimaryKey.set(true);
                    }
                }
                if (tokens.canConsume("PRIMARY", "KEY") || tokens.canConsume("KEY")) {
                    // Always set this column as the primary key
                    column.optional(false); // MySQL primary key columns may not be null
                    isPrimaryKey.set(true);
                }
                if (tokens.canConsume("COMMENT")) {
                    consumeQuotedString();
                }
                if (tokens.canConsume("COLUMN_FORMAT")) {
                    tokens.consumeAnyOf("FIXED", "DYNAMIC", "DEFAULT");
                }
                if (tokens.matches("REFERENCES")) {
                    parseReferenceDefinition(start);
                }
                if (tokens.canConsume("COLLATE")) {
                    tokens.consume(); // name of collation
                }
            }
        }
    }
