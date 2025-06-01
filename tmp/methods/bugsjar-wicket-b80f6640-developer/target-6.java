	protected NumberFormat newNumberFormat(Locale locale)
	{
		NumberFormat numberFormat  = NumberFormat.getIntegerInstance(locale);
		numberFormat.setParseIntegerOnly(true);
		numberFormat.setGroupingUsed(false);
		return numberFormat;
	}
	public NumberFormat getNumberFormat(final Locale locale)
	{
		NumberFormat numberFormat = numberFormats.get(locale);
		if (numberFormat == null)
		{
			numberFormat = newNumberFormat(locale);

			if (numberFormat instanceof DecimalFormat)
			{
				// always try to parse BigDecimals
				((DecimalFormat)numberFormat).setParseBigDecimal(true);
			}

			NumberFormat tmpNumberFormat = numberFormats.putIfAbsent(locale, numberFormat);
			if (tmpNumberFormat != null)
			{
				numberFormat = tmpNumberFormat;
			}
		}
		// return a clone because NumberFormat.get..Instance use a pool
		return (NumberFormat)numberFormat.clone();
	}
	protected abstract NumberFormat newNumberFormat(final Locale locale);

	/**
	 * Parses a value as a String and returns a Number.
	 * 
	 * @param value
	 *            The object to parse (after converting with toString())
	 * @param min
	 *            The minimum allowed value or {@code null} if none
	protected BigDecimal parse(Object value, final BigDecimal min, final BigDecimal max, Locale locale)
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

		BigDecimal bigDecimal;
		if (number instanceof BigDecimal)
		{
			bigDecimal = (BigDecimal)number;
		}
		else
		{
			// should occur rarely, see #getNumberFormat(Locale)
			bigDecimal = new BigDecimal(number.toString());
		}

		if (min != null && bigDecimal.compareTo(min) < 0)
		{
			throw newConversionException("Value cannot be less than " + min, value, locale)
					.setFormat(numberFormat);
		}

		if (max != null && bigDecimal.compareTo(max) > 0)
		{
			throw newConversionException("Value cannot be greater than " + max, value, locale)
					.setFormat(numberFormat);
		}

		return bigDecimal;
	}
	public BigDecimal convertToObject(final String value, final Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		return parse(value, null, null, locale);
	}
	public BigInteger convertToObject(final String value, final Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		final BigDecimal number = parse(value, null, null, locale);

		if (number == null)
		{
			return null;
		}

		return new BigInteger(number.toString());
	}
	public Byte convertToObject(final String value, final Locale locale)
	{
		final BigDecimal number = parse(value, MIN_VALUE, MAX_VALUE, locale);

		if (number == null)
		{
			return null;
		}

		return number.byteValue();
	}
	public Double convertToObject(final String value, final Locale locale)
	{
		final BigDecimal number = parse(value, MIN_VALUE, MAX_VALUE, locale);

		if (number == null)
		{
			return null;
		}

		return number.doubleValue();
	}
