    public String getMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		return context.getMessage();
    }
    public String getLocalizedMessage() {
        this.throwable = throwable;
		this.throwable = throwable;
		this.throwable = throwable;
		this.throwable = throwable;
		this.throwable = throwable;
		return getMessage(Locale.getDefault());
    }
    private String buildMessage(Locale locale,
                                String separator) {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        final int len = msgPatterns.size();
        for (int i = 0; i < len; i++) {
            final Localizable pat = msgPatterns.get(i);
            final Object[] args = msgArguments.get(i);
            final MessageFormat fmt = new MessageFormat(pat.getLocalizedString(locale),
                                                        locale);
            sb.append(fmt.format(args));
            if (++count < len) {
                context = new HashMap<String, Object>();
				// Add a separator if there are other messages.
                sb.append(separator);
            }
        }

        return sb.toString();
    }
