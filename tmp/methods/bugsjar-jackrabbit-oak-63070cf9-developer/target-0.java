    private static Query getQuery(Filter filter, IndexReader reader,
            boolean nonFullTextConstraints, Analyzer analyzer, NodeState indexDefinition) {
        List<Query> qs = new ArrayList<Query>();
        FullTextExpression ft = filter.getFullTextConstraint();
        if (ft == null) {
            // there might be no full-text constraint
            // when using the LowCostLuceneIndexProvider
            // which is used for testing
        } else {
            qs.add(getFullTextQuery(ft, analyzer, reader));
        }
        PropertyRestriction pr = filter.getPropertyRestriction(NATIVE_QUERY_FUNCTION);
        if (pr != null) {
            String query = String.valueOf(pr.first.getValue(pr.first.getType()));
            QueryParser queryParser = new QueryParser(VERSION, "", analyzer);
            if (query.startsWith("mlt?")) {
                String mltQueryString = query.replace("mlt?", "");
                if (reader != null) {
                    Query moreLikeThis = MoreLikeThisHelper.getMoreLikeThis(reader, analyzer, mltQueryString);
                    if (moreLikeThis != null) {
                        qs.add(moreLikeThis);
                    }
                }
            }
            else {
                try {
                    qs.add(queryParser.parse(query));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (nonFullTextConstraints) {
            addNonFullTextConstraints(qs, filter, reader, analyzer,
                    indexDefinition);
        }
        if (qs.size() == 0) {
            return new MatchAllDocsQuery();
        }
        if (qs.size() == 1) {
            return qs.get(0);
        }
        BooleanQuery bq = new BooleanQuery();
        for (Query q : qs) {
            bq.add(q, MUST);
        }
        return bq;
    }
    public Cursor query(Filter filter, NodeState root) {
        if (!isLive(root)) {
            throw new IllegalStateException("Lucene index is not live");
        }
        FullTextExpression ft = filter.getFullTextConstraint();
        Set<String> relPaths = getRelativePaths(ft);
        if (relPaths.size() > 1) {
            return new MultiLuceneIndex(filter, root, relPaths).query();
        }
        String parent = relPaths.size() == 0 ? "" : relPaths.iterator().next();
        // we only restrict non-full-text conditions if there is
        // no relative property in the full-text constraint
        boolean nonFullTextConstraints = parent.isEmpty();
        Directory directory = newDirectory(root);
        QueryEngineSettings settings = filter.getQueryEngineSettings();
        if (directory == null) {
            return newPathCursor(Collections.<String> emptySet(), settings);
        }
        long s = System.currentTimeMillis();
        try {
            try {
                IndexReader reader = DirectoryReader.open(directory);
                try {
                    IndexSearcher searcher = new IndexSearcher(reader);
                    List<LuceneResultRow> rows = new ArrayList<LuceneResultRow>();
                    Query query = getQuery(filter, reader,
                            nonFullTextConstraints, analyzer, getIndexDef(root));

                    // TODO OAK-828
                    HashSet<String> seenPaths = new HashSet<String>();
                    int parentDepth = getDepth(parent);
                    if (query != null) {
                        // OAK-925
                        // TODO how to best avoid loading all entries in memory?
                        // (memory problem and performance problem)
                        TopDocs docs = searcher
                                .search(query, Integer.MAX_VALUE);
                        for (ScoreDoc doc : docs.scoreDocs) {
                            String path = reader.document(doc.doc,
                                    PATH_SELECTOR).get(PATH);
                            if (path != null) {
                                if ("".equals(path)) {
                                    path = "/";
                                }
                                if (!parent.isEmpty()) {
                                    // TODO OAK-828 this breaks node aggregation
                                    // get the base path
                                    // ensure the path ends with the given
                                    // relative path
                                    // if (!path.endsWith("/" + parent)) {
                                    // continue;
                                    // }
                                    path = getAncestorPath(path, parentDepth);
                                    // avoid duplicate entries
                                    if (seenPaths.contains(path)) {
                                        continue;
                                    }
                                    seenPaths.add(path);
                                }

                                LuceneResultRow r = new LuceneResultRow();
                                r.path = path;
                                r.score = doc.score;
                                rows.add(r);
                            }
                        }
                    }
                    LOG.debug("query via {} took {} ms.", this,
                            System.currentTimeMillis() - s);
                    return new LucenePathCursor(rows, settings);
                } finally {
                    reader.close();
                }
            } finally {
                directory.close();
            }
        } catch (IOException e) {
            LOG.warn("query via {} failed.", this, e);
            return newPathCursor(Collections.<String> emptySet(), settings);
        }
    }
    private static void addNonFullTextConstraints(List<Query> qs,
            Filter filter, IndexReader reader, Analyzer analyzer, NodeState indexDefinition) {
        if (!filter.matchesAllTypes()) {
            addNodeTypeConstraints(qs, filter);
        }

        String path = filter.getPath();
        switch (filter.getPathRestriction()) {
        case ALL_CHILDREN:
            if ("/".equals(path)) {
                break;
            }
            if (!path.endsWith("/")) {
                path += "/";
            }
            qs.add(new PrefixQuery(newPathTerm(path)));
            break;
        case DIRECT_CHILDREN:
            if (!path.endsWith("/")) {
                path += "/";
            }
            qs.add(new PrefixQuery(newPathTerm(path)));
            break;
        case EXACT:
            qs.add(new TermQuery(newPathTerm(path)));
            break;
        case PARENT:
            if (denotesRoot(path)) {
                // there's no parent of the root node
                // we add a path that can not possibly occur because there
                // is no way to say "match no documents" in Lucene
                qs.add(new TermQuery(new Term(FieldNames.PATH, "///")));
            } else {
                qs.add(new TermQuery(newPathTerm(getParentPath(path))));
            }
            break;
        case NO_RESTRICTION:
            break;
        }

        for (PropertyRestriction pr : filter.getPropertyRestrictions()) {

            if (pr.first == null && pr.last == null) {
                // ignore property existence checks, Lucene can't to 'property
                // is not null' queries (OAK-1208)
                continue;
            }

            // check excluded properties and types
            if (isExcludedProperty(pr, indexDefinition)) {
                continue;
            }

            String name = pr.propertyName;
            if ("rep:excerpt".equals(name)) {
                continue;
            }
            if (JCR_PRIMARYTYPE.equals(name)) {
                continue;
            }

            if (skipTokenization(name)) {
                qs.add(new TermQuery(new Term(name, pr.first
                        .getValue(STRING))));
                continue;
            }

            String first = null;
            String last = null;
            boolean isLike = pr.isLike;

            // TODO what to do with escaped tokens?
            if (pr.first != null) {
                first = pr.first.getValue(STRING);
                first = first.replace("\\", "");
            }
            if (pr.last != null) {
                last = pr.last.getValue(STRING);
                last = last.replace("\\", "");
            }

            if (isLike) {
                first = first.replace('%', WildcardQuery.WILDCARD_STRING);
                first = first.replace('_', WildcardQuery.WILDCARD_CHAR);

                int indexOfWS = first.indexOf(WildcardQuery.WILDCARD_STRING);
                int indexOfWC = first.indexOf(WildcardQuery.WILDCARD_CHAR);
                int len = first.length();

                if (indexOfWS == len || indexOfWC == len) {
                    // remove trailing "*" for prefixquery
                    first = first.substring(0, first.length() - 1);
                    if (JCR_PATH.equals(name)) {
                        qs.add(new PrefixQuery(newPathTerm(first)));
                    } else {
                        qs.add(new PrefixQuery(new Term(name, first)));
                    }
                } else {
                    if (JCR_PATH.equals(name)) {
                        qs.add(new WildcardQuery(newPathTerm(first)));
                    } else {
                        qs.add(new WildcardQuery(new Term(name, first)));
                    }
                }
                continue;
            }

            if (first != null && first.equals(last) && pr.firstIncluding
                    && pr.lastIncluding) {
                if (JCR_PATH.equals(name)) {
                    qs.add(new TermQuery(newPathTerm(first)));
                } else {
                    if ("*".equals(name)) {
                        addReferenceConstraint(first, qs, reader);
                    } else {
                        for (String t : tokenize(first, analyzer)) {
                            qs.add(new TermQuery(new Term(name, t)));
                        }
                    }
                }
                continue;
            }

            first = tokenizeAndPoll(first, analyzer);
            last = tokenizeAndPoll(last, analyzer);
            qs.add(TermRangeQuery.newStringRange(name, first, last,
                    pr.firstIncluding, pr.lastIncluding));
        }
    }
    public String getPlan(Filter filter, NodeState root) {
        FullTextExpression ft = filter.getFullTextConstraint();
        Set<String> relPaths = getRelativePaths(ft);
        if (relPaths.size() > 1) {
            return new MultiLuceneIndex(filter, root, relPaths).getPlan();
        }
        String parent = relPaths.size() == 0 ? "" : relPaths.iterator().next();
        // we only restrict non-full-text conditions if there is
        // no relative property in the full-text constraint
        boolean nonFullTextConstraints = parent.isEmpty();
        String plan = getQuery(filter, null, nonFullTextConstraints, analyzer, getIndexDef(root)) + " ft:(" + ft + ")";
        if (!parent.isEmpty()) {
            plan += " parent:" + parent;
        }
        return plan;
    }
    private static boolean isExcludedProperty(PropertyRestriction pr,
            NodeState definition) {
        String name = pr.propertyName;
        if (name.contains("/")) {
            // lucene cannot handle child-level property restrictions
            return true;
        }

        // check name
        for (String e : definition.getStrings(EXCLUDE_PROPERTY_NAMES)) {
            if (e.equalsIgnoreCase(name)) {
                return true;
            }
        }

        // check type
        Integer type = null;
        if (pr.first != null) {
            type = pr.first.getType().tag();
        } else if (pr.last != null) {
            type = pr.last.getType().tag();
        } else if (pr.list != null && !pr.list.isEmpty()) {
            type = pr.list.get(0).getType().tag();
        }
        if (type != null) {
            boolean isIn = false;
            for (String e : definition.getStrings(INCLUDE_PROPERTY_TYPES)) {
                if (PropertyType.valueFromName(e) == type) {
                    isIn = true;
                }
            }
            if (!isIn) {
                return true;
            }
        }
        return false;
    }
