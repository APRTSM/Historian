    public static boolean isAvailableLocale(Locale locale) {
        if (cAvailableLocaleSet == null) {
    return false;
}
return cAvailableLocaleSet.contains(locale);
    }
