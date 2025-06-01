    protected void parseAlterSpecification(Marker start, TableEditor table, Consumer<TableId> newTableName) {
        parseTableOptions(start, table);
        if (tokens.canConsume("ADD")) {
            if (tokens.matches("COLUMN", "(") || tokens.matches('(')) {
                tokens.canConsume("COLUMN");
                parseCreateDefinitionList(start, table);
            } else if (tokens.canConsume("PARTITION", "(")) {
                parsePartitionDefinition(start, table);
                tokens.consume(')');
            } else {
                parseCreateDefinition(start, table, true);
            }
        } else if (tokens.canConsume("DROP")) {
            if (tokens.canConsume("PRIMARY", "KEY")) {
                table.setPrimaryKeyNames();
            } else if (tokens.canConsume("FOREIGN", "KEY")) {
                tokens.consume(); // foreign key symbol
            } else if (tokens.canConsumeAnyOf("INDEX", "KEY")) {
                tokens.consume(); // index name
            } else if (tokens.canConsume("PARTITION")) {
                parsePartitionNames(start);
            } else {
                if(!isNextTokenQuotedIdentifier()) {
                    tokens.canConsume("COLUMN");
                }
                String columnName = parseColumnName();
                table.removeColumn(columnName);
            }
        } else if (tokens.canConsume("ALTER")) {
            if (!isNextTokenQuotedIdentifier()) {
                tokens.canConsume("COLUMN");
            }
            tokens.consume(); // column name
            if (!tokens.canConsume("DROP", "DEFAULT")) {
                tokens.consume("SET");
                parseDefaultClause(start);
            }
        } else if (tokens.canConsume("CHANGE")) {
            if (!isNextTokenQuotedIdentifier()) {
                tokens.canConsume("COLUMN");
            }
            String oldName = parseColumnName();
            String newName = parseColumnName();
            parseCreateColumn(start, table, oldName, newName);
        } else if (tokens.canConsume("MODIFY")) {
            if (!isNextTokenQuotedIdentifier()) {
                tokens.canConsume("COLUMN");
            }
            String columnName = parseColumnName();
            parseCreateColumn(start, table, columnName, null);
        } else if (tokens.canConsumeAnyOf("ALGORITHM", "LOCK")) {
            tokens.canConsume('=');
            tokens.consume();
        } else if (tokens.canConsume("DISABLE", "KEYS")
                || tokens.canConsume("ENABLE", "KEYS")) {} else if (tokens.canConsume("RENAME", "INDEX")
                        || tokens.canConsume("RENAME", "KEY")) {
            tokens.consume(); // old
            tokens.consume("TO");
            tokens.consume(); // new
        } else if (tokens.canConsume("RENAME")) {
            tokens.canConsumeAnyOf("AS", "TO");
            TableId newTableId = parseQualifiedTableName(start);
            newTableName.accept(newTableId);
        } else if (tokens.canConsume("ORDER", "BY")) {
            consumeCommaSeparatedValueList(start); // this should not affect the order of the columns in the table
        } else if (tokens.canConsume("CONVERT", "TO", "CHARACTER", "SET")
                || tokens.canConsume("CONVERT", "TO", "CHARSET")) {
            tokens.consume(); // charset name
            if (tokens.canConsume("COLLATE")) {
                tokens.consume(); // collation name
            }
        } else if (tokens.canConsume("CHARACTER", "SET")
                || tokens.canConsume("CHARSET")
                || tokens.canConsume("DEFAULT", "CHARACTER", "SET")
                || tokens.canConsume("DEFAULT", "CHARSET")) {
            tokens.canConsume('=');
            String charsetName = tokens.consume(); // charset name
            table.setDefaultCharsetName(charsetName);
            if (tokens.canConsume("COLLATE")) {
                tokens.canConsume('=');
                tokens.consume(); // collation name (ignored)
            }
        } else if (tokens.canConsume("DISCARD", "TABLESPACE") || tokens.canConsume("IMPORT", "TABLESPACE")) {
            // nothing
        } else if (tokens.canConsume("FORCE")) {
            // nothing
        } else if (tokens.canConsume("WITH", "VALIDATION") || tokens.canConsume("WITHOUT", "VALIDATION")) {
            // nothing
        } else if (tokens.canConsume("DISCARD", "PARTITION") || tokens.canConsume("IMPORT", "PARTITION")) {
            if (!tokens.canConsume("ALL")) {
                tokens.consume(); // partition name
            }
            tokens.consume("TABLESPACE");
        } else if (tokens.canConsume("COALLESCE", "PARTITION")) {
            tokens.consume(); // number
        } else if (tokens.canConsume("REORGANIZE", "PARTITION")) {
            parsePartitionNames(start);
            tokens.consume("INTO", "(");
            parsePartitionDefinition(start, table);
            tokens.consume(')');
        } else if (tokens.canConsume("EXCHANGE", "PARTITION")) {
            tokens.consume(); // partition name
            tokens.consume("WITH", "TABLE");
            parseSchemaQualifiedName(start); // table name
            if (tokens.canConsumeAnyOf("WITH", "WITHOUT")) {
                tokens.consume("VALIDATION");
            }
        } else if (tokens.matches(TokenStream.ANY_VALUE, "PARTITION")) {
            tokens.consumeAnyOf("TRUNCATE", "CHECK", "ANALYZE", "OPTIMIZE", "REBUILD", "REPAIR");
            tokens.consume("PARTITION");
            if (!tokens.canConsume("ALL")) {
                parsePartitionNames(start);
            }
        } else if (tokens.canConsume("REMOVE", "PARTITIONING")) {
            // nothing
        } else if (tokens.canConsume("UPGRADE", "PARTITIONING")) {
            // nothing
        }
    }
