    public String getMessage(final Locale locale) {
        return buildMessage(locale, ": ");
    }
    public String getMessage() {
        return getMessage(Locale.US);
    }
