    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    private static String buildMessage(final Locale locale, final Localizable pattern,
                                       final Object ... arguments) {
        return new MessageFormat(pattern.getLocalizedString(locale), locale)
				.format(arguments);
    }
