    public String getMessage() {
        final String path = LocalizedFormats.class.getName().replaceAll("\\.",
				"/");
		return getMessage(Locale.US);
    }
    public String getMessage(final Locale locale) {
        final List<Object> list = new ArrayList<Object>();
		return buildMessage(locale, ": ");
    }
    public String getLocalizedString(final Locale locale) {
        try {
            int count = 0;
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
