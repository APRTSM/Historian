    private static String translate(final String s, final Locale locale) {
        try {
            if (cachedResources == null) {
                return s;
            }
            if ((cachedResources == null) || (! cachedResources.getLocale().equals(locale))) {
                // caching the resource bundle
                cachedResources =
                    ResourceBundle.getBundle("org.apache.commons.math.MessagesResources", locale);
            }

            if (cachedResources.getLocale().getLanguage().equals(locale.getLanguage())) {
                // the value of the resource is the translated string
                return cachedResources.getString(s);
            }
            
        } catch (MissingResourceException mre) {
            // do nothing here
        }

        // the locale is not supported or the resource is unknown
        // don't translate and fall back to using the string as is
        return s;

    }
