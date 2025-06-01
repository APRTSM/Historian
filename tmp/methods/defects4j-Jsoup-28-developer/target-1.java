    public static boolean isBaseNamedEntity(String name) {
        return base.containsKey(name);
    }
    static String unescape(String string, boolean strict) {
        return Parser.unescapeEntities(string, strict);
    }
    public static String unescapeEntities(String string, boolean inAttribute) {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(string), ParseErrorList.noTracking());
        return tokeniser.unescapeEntities(inAttribute);
    }
