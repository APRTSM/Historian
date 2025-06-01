    public String getMessage(final Locale locale) {
        return buildMessage(locale, ": ");
    }
    public String getMessage() {
        return getMessage(Locale.US);
    }
    public String getLocalizedString(final Locale locale) {
        try {
            final String path = LocalizedFormats.class.getName().replaceAll("\\.", "/");
            ResourceBundle bundle =
                    ResourceBundle.getBundle("assets/" + path, locale);
            if (bundle.getLocale().getLanguage().equals(locale.getLanguage())) {
                // the value of the resource is the translated format
                return bundle.getString(toString());
            }

        } catch (MissingResourceException mre) {
            // do nothing here
        }

        // either the locale is not supported or the resource is unknown
        // don't translate and fall back to using the source format
        return sourceFormat;

    }
