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
        if (pattern != null) {
											return new MessageFormat(
													pattern.getLocalizedString(locale),
													locale).format(arguments);
										}
		return new MessageFormat(pattern.getLocalizedString(locale), locale).format(arguments);
    }
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }
