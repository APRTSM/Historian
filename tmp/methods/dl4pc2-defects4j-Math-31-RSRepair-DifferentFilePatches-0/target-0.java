    public String getMessage(final Locale locale) {
        final int len = context.keySet().size();
		return buildMessage(locale, ": ");
    }
