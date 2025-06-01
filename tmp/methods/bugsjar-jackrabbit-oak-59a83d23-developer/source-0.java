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
                    if (isVirtualRow()) {
                        return sub;
                    } else if (PathUtils.isAbsolute(sub)) {
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
                        return PropertyValues.newString(currentRow.suggestion);
                    }
                    if (QueryImpl.OAK_SCORE_EXPLANATION.equals(columnName)) {
                        return PropertyValues.newString(currentRow.explanation);
                    }
                    if (QueryImpl.REP_EXCERPT.equals(columnName)) {
                        return PropertyValues.newString(currentRow.excerpt);
                    }
                    if (columnName.startsWith(QueryImpl.REP_FACET)) {
                        String facetFieldName = FacetHelper.parseFacetField(columnName);
                        Facets facets = currentRow.facets;
                        try {
                            if (facets != null) {
                                FacetResult topChildren = facets.getTopChildren(10, facetFieldName);
                                if (topChildren != null) {
                                    JsopWriter writer = new JsopBuilder();
                                    writer.object();
                                    for (LabelAndValue lav : topChildren.labelValues) {
                                        writer.key(lav.label).value(lav.value.intValue());
                                    }
                                    writer.endObject();
                                    return PropertyValues.newString(writer.toString());
                                } else {
                                    return null;
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return pathRow.getValue(columnName);
                }

            };
        }
