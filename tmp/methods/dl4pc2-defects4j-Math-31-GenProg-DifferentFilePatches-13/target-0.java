    public String getMessage(final Locale locale) {
        this.throwable = throwable;
		return buildMessage(locale, ": ");
    }
