	public final void setNumberFormat(final Locale locale, final NumberFormat numberFormat)
	{
		if (numberFormat instanceof DecimalFormat)
		{
			((DecimalFormat)numberFormat).setParseBigDecimal(true);
		}

		numberFormats.put(locale, numberFormat);
	}
	public NumberFormat getNumberFormat(final Locale locale)
	{
		NumberFormat numberFormat = numberFormats.get(locale);
		if (numberFormat == null)
		{
			numberFormat = newNumberFormat(locale);
			setNumberFormat(locale, numberFormat);
		}
		return (NumberFormat)numberFormat.clone();
	}
	public NumberFormat getNumberFormat(final Locale locale)
	{
		NumberFormat numberFormat = numberFormats.get(locale);
		if (numberFormat == null)
		{
			numberFormat = NumberFormat.getIntegerInstance(locale);
			numberFormat.setParseIntegerOnly(true);
			numberFormat.setGroupingUsed(false);
			NumberFormat tmpNumberFormat = numberFormats.putIfAbsent(locale, numberFormat);
			if (tmpNumberFormat != null)
			{
				numberFormat = tmpNumberFormat;
			}
		}
		return (NumberFormat)numberFormat.clone();
	}
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
			// Convert spaces to no-break space (U+00A0) as required by Java formats:
			// http://bugs.sun.com/view_bug.do?bug_id=4510618
			value = ((String)value).replaceAll("(\\d+)\\s(?=\\d)", "$1\u00A0");
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
	public abstract NumberFormat getNumberFormat(Locale locale);
