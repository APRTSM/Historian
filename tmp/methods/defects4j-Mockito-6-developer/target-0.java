    public static <T> T any() {
        return (T) reportMatcher(Any.ANY).returnNull();
    }
    public static <T> Set<T> anySetOf(Class<T> clazz) {
        return anySet();
    }
    public static int anyInt() {
        return reportMatcher(new InstanceOf(Integer.class)).returnZero();
    }
    public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
        return anyCollection();
    }    
    public static short anyShort() {
        return reportMatcher(new InstanceOf(Short.class)).returnZero();
    }
    public static <T> T any(Class<T> clazz) {
        return (T) reportMatcher(new InstanceOf(clazz)).returnFor(clazz);
    }
    public static boolean anyBoolean() {
        return reportMatcher(new InstanceOf(Boolean.class)).returnFalse();
    }
    public static String anyString() {
        return reportMatcher(new InstanceOf(String.class)).returnString();
    }
    public static Map anyMap() {
        return reportMatcher(new InstanceOf(Map.class)).returnMap();
    }
    public static long anyLong() {
        return reportMatcher(new InstanceOf(Long.class)).returnZero();
    }
    public static byte anyByte() {
        return reportMatcher(new InstanceOf(Byte.class)).returnZero();
    }
    public static List anyList() {
        return reportMatcher(new InstanceOf(List.class)).returnList();
    }    
    public static float anyFloat() {
        return reportMatcher(new InstanceOf(Float.class)).returnZero();
    }
    public static <T> List<T> anyListOf(Class<T> clazz) {
        return anyList();
    }    
    public static <K, V>  Map<K, V> anyMapOf(Class<K> keyClazz, Class<V> valueClazz) {
        return anyMap();
    }
    public static char anyChar() {
        return reportMatcher(new InstanceOf(Character.class)).returnChar();
    }
    public static <T> T anyObject() {
        return (T) reportMatcher(new InstanceOf(Object.class)).returnNull();
    }
    public static double anyDouble() {
        return reportMatcher(new InstanceOf(Double.class)).returnZero();
    }
    public static Collection anyCollection() {
        return reportMatcher(new InstanceOf(Collection.class)).returnList();
    }    
    public static Set anySet() {
        return reportMatcher(new InstanceOf(Set.class)).returnSet();
    }
