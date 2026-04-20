    public static String getShortClassName(Class<?> cls) {
// start of generated patch
if(cls==null){
return StringUtils.EMPTY;
}
return getShortCanonicalName(cls.getName());
// end of generated patch
/* start of original code
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return getShortClassName(cls.getName());
 end of original code*/
    }
    public static String getPackageName(Class<?> cls) {
// start of generated patch
if(cls==null){
return StringUtils.EMPTY;
}
return getPackageCanonicalName(cls.getName());
// end of generated patch
/* start of original code
        if (cls == null) {
            return StringUtils.EMPTY;
        }
        return getPackageName(cls.getName());
 end of original code*/
    }
