if(locale == null) {
    return false;
}
return Arrays.asList(Locale.getAvailableLocales()).contains(locale);