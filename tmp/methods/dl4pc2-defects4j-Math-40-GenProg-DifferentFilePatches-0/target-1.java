    public String getLocalizedMessage() {
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
			final MessageFormat fmt = new MessageFormat(
					pat.getLocalizedString(locale), locale);
			sb.append(fmt.format(args));
			if (++count < len) {
				sb.append(separator);
			}
		}

        return sb.toString();
    }
    public String getLocalizedMessage() {
        return context.getMessage();
    }
