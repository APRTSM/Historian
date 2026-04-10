public static boolean isAvailableLocale(Locale locale) {
    return locale != null && cAvailableLocaleSet != null && cAvailableLocaleSet.contains(locale);
}
