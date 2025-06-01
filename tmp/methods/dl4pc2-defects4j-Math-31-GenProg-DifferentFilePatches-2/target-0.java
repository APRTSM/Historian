    public String getMessage(final Locale locale) {
        final String path = LocalizedFormats.class.getName().replaceAll("\\.",
				"/");
		return buildMessage(locale, ": ");
    }
