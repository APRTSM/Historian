    private static Term[] extractMatchingTokens(IndexReader reader, String token) {
        if (reader == null) {
            // getPlan call
            return null;
        }

        try {
            List<Term> terms = new ArrayList<Term>();
            Terms t = MultiFields.getTerms(reader, FieldNames.FULLTEXT);
            Automaton a = WildcardQuery.toAutomaton(newFulltextTerm(token));
            CompiledAutomaton ca = new CompiledAutomaton(a);
            TermsEnum te = ca.getTermsEnum(t);
            BytesRef text;
            while ((text = te.next()) != null) {
                terms.add(newFulltextTerm(text.utf8ToString()));
            }
            return terms.toArray(new Term[terms.size()]);
        } catch (IOException e) {
            LOG.error("Building fulltext query failed", e.getMessage());
            return null;
        }
    }
    private static boolean hasFulltextToken(List<String> tokens) {
        for (String token : tokens) {
            if (hasFulltextToken(token)) {
                return true;
            }
        }
        return false;
    }
    static List<String> tokenize(String text, Analyzer analyzer) {
        List<String> tokens = new ArrayList<String>();
        TokenStream stream = null;
        try {
            stream = analyzer.tokenStream(FieldNames.FULLTEXT,
                    new StringReader(text));
            CharTermAttribute termAtt = stream
                    .addAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAtt = stream
                    .addAttribute(OffsetAttribute.class);
            // TypeAttribute type = stream.addAttribute(TypeAttribute.class);

            stream.reset();

            int poz = 0;
            boolean hasFulltextToken = false;
            StringBuilder token = new StringBuilder();
            while (stream.incrementToken()) {
                String term = termAtt.toString();
                int start = offsetAtt.startOffset();
                int end = offsetAtt.endOffset();
                if (start > poz) {
                    for (int i = poz; i < start; i++) {
                        for (char c : fulltextTokens) {
                            if (c == text.charAt(i)) {
                                token.append(c);
                                hasFulltextToken = true;
                            }
                        }
                    }
                }
                poz = end;
                if (hasFulltextToken) {
                    token.append(term);
                    hasFulltextToken = false;
                } else {
                    if (token.length() > 0) {
                        tokens.add(token.toString());
                    }
                    token = new StringBuilder();
                    token.append(term);
                }
            }
            // consume to the end of the string
            if (poz < text.length()) {
                for (int i = poz; i < text.length(); i++) {
                    for (char c : fulltextTokens) {
                        if (c == text.charAt(i)) {
                            token.append(c);
                        }
                    }
                }
            }
            if (token.length() > 0) {
                tokens.add(token.toString());
            }
            stream.end();
        } catch (IOException e) {
            LOG.error("Building fulltext query failed", e.getMessage());
            return null;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return tokens;
    }
    static Query tokenToQuery(String text, Analyzer analyzer, IndexReader reader) {
        if (analyzer == null) {
            return null;
        }
        List<String> tokens = new ArrayList<String>();
        tokens = tokenize(text, analyzer);

        if (tokens.isEmpty()) {
            // TODO what should be returned in the case there are no tokens?
            return new BooleanQuery();
        }
        if (tokens.size() == 1) {
            String token = tokens.iterator().next();
            if (hasFulltextToken(token)) {
                return new WildcardQuery(newFulltextTerm(token));
            } else {
                return new TermQuery(newFulltextTerm(token));
            }
        } else {
            if (hasFulltextToken(tokens)) {
                MultiPhraseQuery mpq = new MultiPhraseQuery();
                for(String token: tokens){
                    if (hasFulltextToken(token)) {
                        Term[] terms = extractMatchingTokens(reader, token);
                        if (terms != null && terms.length > 0) {
                            mpq.add(terms);
                        }
                    } else {
                        mpq.add(newFulltextTerm(token));
                    }
                }
                return mpq;
            } else {
                PhraseQuery pq = new PhraseQuery();
                for (String t : tokens) {
                    pq.add(newFulltextTerm(t));
                }
                return pq;
            }
        }
    }
    private static Query getQuery(Filter filter, IndexReader reader,
            boolean nonFullTextConstraints, Analyzer analyzer) {
        List<Query> qs = new ArrayList<Query>();
        FullTextExpression ft = filter.getFullTextConstraint();
        if (ft == null) {
            // there might be no full-text constraint
            // when using the LowCostLuceneIndexProvider
            // which is used for testing
        } else {
            qs.add(getFullTextQuery(ft, analyzer, reader));
        }
        if (nonFullTextConstraints) {
            addNonFullTextConstraints(qs, filter, reader);
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
    static Query getFullTextQuery(FullTextExpression ft, final Analyzer analyzer, final IndexReader reader) {
        // a reference to the query, so it can be set in the visitor
        // (a "non-local return")
        final AtomicReference<Query> result = new AtomicReference<Query>();
        ft.accept(new FullTextVisitor() {

            @Override
            public boolean visit(FullTextOr or) {
                BooleanQuery q = new BooleanQuery();
                for (FullTextExpression e : or.list) {
                    Query x = getFullTextQuery(e, analyzer, reader);
                    q.add(x, SHOULD);
                }
                result.set(q);
                return true;
            }

            @Override
            public boolean visit(FullTextAnd and) {
                BooleanQuery q = new BooleanQuery();
                for (FullTextExpression e : and.list) {
                    Query x = getFullTextQuery(e, analyzer, reader);
                    // Lucene can't deal with "must(must_not(x))"
                    if (x instanceof BooleanQuery) {
                        BooleanQuery bq = (BooleanQuery) x;
                        for (BooleanClause c : bq.clauses()) {
                            q.add(c);
                        }
                    } else {
                        q.add(x, MUST);
                    }
                }
                result.set(q);
                return true;
            }

            @Override
            public boolean visit(FullTextTerm term) {
                String p = term.getPropertyName();
                if (p != null && p.indexOf('/') >= 0) {
                    // do not add constraints on child nodes properties
                    p = "*";
                }
                Query q = tokenToQuery(term.getText(), analyzer, reader);
                if (q == null) {
                    return false;
                }
                String boost = term.getBoost();
                if (boost != null) {
                    q.setBoost(Float.parseFloat(boost));
                }
                if (term.isNot()) {
                    BooleanQuery bq = new BooleanQuery();
                    bq.add(q, MUST_NOT);
                    result.set(bq);
                } else {
                    result.set(q);
                }
                return true;
            }
        });
        return result.get();
    }
    private static boolean hasFulltextToken(String token) {
        for (char c : fulltextTokens) {
            if (token.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }
