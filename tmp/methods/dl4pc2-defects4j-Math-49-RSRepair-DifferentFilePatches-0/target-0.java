    private static String buildMessage(final Locale locale, final Localizable pattern,
                                       final Object ... arguments) {
        if (pattern != null) {
											return new MessageFormat(
													pattern.getLocalizedString(locale),
													locale).format(arguments);
										}
		return new MessageFormat(pattern.getLocalizedString(locale), locale).format(arguments);
    }
