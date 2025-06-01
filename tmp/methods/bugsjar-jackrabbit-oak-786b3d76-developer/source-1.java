        public IndexRow next() {
            final IndexRow pathRow = pathCursor.next();
            return new IndexRow() {

                @Override
                public boolean isVirtualRow() {
                    return getPath() == null;
                }

                @Override
                public String getPath() {
                    return pathRow.getPath();
                }

                @Override
                public PropertyValue getValue(String columnName) {
                    // overlay the score
                    if (QueryImpl.JCR_SCORE.equals(columnName)) {
                        return PropertyValues.newDouble(currentRow.score);
                    }
                    if (QueryImpl.REP_SPELLCHECK.equals(columnName) || QueryImpl.REP_SUGGEST.equals(columnName)) {
                        return PropertyValues.newString(Iterables.toString(currentRow.suggestWords));
                    }
                    return pathRow.getValue(columnName);
                }

            };
        }
        public IndexRow next() {
            final IndexRow pathRow = pathCursor.next();
            return new IndexRow() {

                @Override
                public boolean isVirtualRow() {
                    return getPath() == null;
                }

                @Override
                public String getPath() {
                    String sub = pathRow.getPath();
                    if (PathUtils.isAbsolute(sub)) {
                        return pathPrefix + sub;
                    } else {
                        return PathUtils.concat(pathPrefix, sub);
                    }
                }

                @Override
                public PropertyValue getValue(String columnName) {
                    // overlay the score
                    if (QueryImpl.JCR_SCORE.equals(columnName)) {
                        return PropertyValues.newDouble(currentRow.score);
                    }
                    if (QueryImpl.REP_SPELLCHECK.equals(columnName) || QueryImpl.REP_SUGGEST.equals(columnName)) {
                        return PropertyValues.newString(Iterables.toString(currentRow.suggestWords));
                    }
                    return pathRow.getValue(columnName);
                }

            };
        }
