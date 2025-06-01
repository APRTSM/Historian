    private static Term[] extractMatchingTokens(IndexReader reader, String fieldName, String token) {
        if (reader == null) {
            // getPlan call
            return null;
        }

        try {
            List<Term> terms = new ArrayList<Term>();
            Term onTerm = newFulltextTerm(token, fieldName);
            Terms t = MultiFields.getTerms(reader, onTerm.field());
            Automaton a = WildcardQuery.toAutomaton(onTerm);
            CompiledAutomaton ca = new CompiledAutomaton(a);
            TermsEnum te = ca.getTermsEnum(t);
            BytesRef text;
            while ((text = te.next()) != null) {
                terms.add(newFulltextTerm(text.utf8ToString(), fieldName));
            }
            return terms.toArray(new Term[terms.size()]);
        } catch (IOException e) {
            LOG.error("Building fulltext query failed", e.getMessage());
            return null;
        }
    }
    static Query tokenToQuery(String text, String fieldName, Analyzer analyzer, IndexReader reader) {
        if (analyzer == null) {
            return null;
        }
        List<String> tokens = tokenize(text, analyzer);

        if (tokens.isEmpty()) {
            // TODO what should be returned in the case there are no tokens?
            return new BooleanQuery();
        }
        if (tokens.size() == 1) {
            String token = tokens.iterator().next();
            if (hasFulltextToken(token)) {
                return new WildcardQuery(newFulltextTerm(token, fieldName));
            } else {
                return new TermQuery(newFulltextTerm(token, fieldName));
            }
        } else {
            if (hasFulltextToken(tokens)) {
                MultiPhraseQuery mpq = new MultiPhraseQuery();
                for(String token: tokens){
                    if (hasFulltextToken(token)) {
                        Term[] terms = extractMatchingTokens(reader, fieldName, token);
                        if (terms != null && terms.length > 0) {
                            mpq.add(terms);
                        }
                    } else {
                        mpq.add(newFulltextTerm(token, fieldName));
                    }
                }
                return mpq;
            } else {
                PhraseQuery pq = new PhraseQuery();
                for (String t : tokens) {
                    pq.add(newFulltextTerm(t, fieldName));
                }
                return pq;
            }
        }
    }
