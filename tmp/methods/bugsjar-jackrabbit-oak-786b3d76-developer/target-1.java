        public IndexRow next() {
            final IndexRow pathRow = pathCursor.next();
            return new IndexRow() {

                @Override
                public boolean isVirtualRow() {
                    return currentRow.isVirtual;
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
        LuceneResultRow(Iterable<String> suggestWords) {
            this.isVirtual = true;
            this.path = "/";
            this.score = 1.0d;
            this.suggestWords = suggestWords;
        }
        LuceneResultRow(String path, double score) {
            this.isVirtual = false;
            this.path = path;
            this.score = score;
            this.suggestWords = Collections.emptySet();
        }
        public IndexRow next() {
            final IndexRow pathRow = pathCursor.next();
            return new IndexRow() {

                @Override
                public boolean isVirtualRow() {
                    return currentRow.isVirutal;
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
        LuceneResultRow(Iterable<String> suggestWords) {
            this.isVirutal = true;
            this.path = "/";
            this.score = 1.0d;
            this.suggestWords = suggestWords;
        }
        LuceneResultRow(String path, double score) {
            this.isVirutal = false;
            this.path = path;
            this.score = score;
            this.suggestWords = Collections.emptySet();
        }
