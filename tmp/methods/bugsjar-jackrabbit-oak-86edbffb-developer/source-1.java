    protected TokenStreamComponents createComponents(final String fieldName,
            final Reader reader) {
        WhitespaceTokenizer src = new WhitespaceTokenizer(matchVersion, reader);
        TokenStream tok = new LowerCaseFilter(matchVersion, src);
        tok = new WordDelimiterFilter(tok,
                WordDelimiterFilter.GENERATE_WORD_PARTS
                        | WordDelimiterFilter.STEM_ENGLISH_POSSESSIVE
                        | WordDelimiterFilter.GENERATE_NUMBER_PARTS, null);

        return new TokenStreamComponents(src, tok);
    }
