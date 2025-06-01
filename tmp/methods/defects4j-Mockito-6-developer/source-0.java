    public static <T> T any() {
        return (T) anyObject();
    }
    public static <T> Set<T> anySetOf(Class<T> clazz) {
        return (Set) reportMatcher(Any.ANY).returnSet();
    }
    public static int anyInt() {
        return reportMatcher(Any.ANY).returnZero();
    }
    public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
        return (Collection) reportMatcher(Any.ANY).returnList();
    }    
    public static short anyShort() {
        return reportMatcher(Any.ANY).returnZero();
    }
    public static <T> T any(Class<T> clazz) {
        return (T) reportMatcher(Any.ANY).returnFor(clazz);
    }
    public static boolean anyBoolean() {
        return reportMatcher(Any.ANY).returnFalse();
    }
    public static String anyString() {
        return reportMatcher(Any.ANY).returnString();
    }
    public static Map anyMap() {
        return reportMatcher(Any.ANY).returnMap();
    }
    public static long anyLong() {
        return reportMatcher(Any.ANY).returnZero();
    }
    public static byte anyByte() {
        return reportMatcher(Any.ANY).returnZero();
    }
    public static List anyList() {
        return reportMatcher(Any.ANY).returnList();
    }    
    public static float anyFloat() {
        return reportMatcher(Any.ANY).returnZero();
    }
    public static <T> List<T> anyListOf(Class<T> clazz) {
        return (List) reportMatcher(Any.ANY).returnList();
    }    
    public static <K, V>  Map<K, V> anyMapOf(Class<K> keyClazz, Class<V> valueClazz) {
        return reportMatcher(Any.ANY).returnMap();
    }
    public static char anyChar() {
        return reportMatcher(Any.ANY).returnChar();
    }
    public static <T> T anyObject() {
        return (T) reportMatcher(Any.ANY).returnNull();
    }
    public static double anyDouble() {
        return reportMatcher(Any.ANY).returnZero();
    }
    public static Collection anyCollection() {
        return reportMatcher(Any.ANY).returnList();
    }    
    public static Set anySet() {
        return reportMatcher(Any.ANY).returnSet();
    }
