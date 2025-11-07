    public static String getShortClassName(Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return org.apache.commons.lang.ClassUtils.getShortCanonicalName(cls.getName());

    }
    public static String getPackageName(Class<?> cls) {
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return org.apache.commons.lang.ClassUtils.getPackageCanonicalName(cls.getName());

    }
