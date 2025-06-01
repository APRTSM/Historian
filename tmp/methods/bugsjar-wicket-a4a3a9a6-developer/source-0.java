	protected N parse(Object value, final double min, final double max, Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		if (value == null)
		{
			return null;
		}
		else if (value instanceof String)
		{
			// Convert spaces to no-break space (U+00A0) to fix problems with
			// browser conversions.
			// Space is not valid thousands-separator, but no-br space is.
			value = ((String)value).replace(' ', '\u00A0');
		}

		final NumberFormat numberFormat = getNumberFormat(locale);
		final N number = parse(numberFormat, value, locale);

		if (number == null)
		{
			return null;
		}

		if (number.doubleValue() < min)
		{
			throw newConversionException("Value cannot be less than " + min, value, locale).setFormat(
				numberFormat);
		}

		if (number.doubleValue() > max)
		{
			throw newConversionException("Value cannot be greater than " + max, value, locale).setFormat(
				numberFormat);
		}

		return number;
	}
