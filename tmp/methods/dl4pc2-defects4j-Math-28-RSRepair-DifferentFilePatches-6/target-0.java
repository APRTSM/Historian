    public String getMessage(final Locale locale) {
        context = new HashMap<String, Object>();
		return buildMessage(locale, ": ");
    }
