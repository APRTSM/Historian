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
	public BigDecimal convertToObject(final String value, final Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		final Number number = parse(value, -Double.MAX_VALUE, Double.MAX_VALUE, locale);

		if (number instanceof BigDecimal)
		{
			return (BigDecimal)number;
		}
		else if (number instanceof Double)
		{
			// See link why the String is preferred for doubles
			// http://java.sun.com/j2se/1.4.2/docs/api/java/math/BigDecimal.html#BigDecimal%28double%29
			return new BigDecimal(Double.toString(number.doubleValue()));
		}
		else if (number instanceof Long)
		{
			return new BigDecimal(number.longValue());
		}
		else if (number instanceof Float)
		{
			return new BigDecimal(number.floatValue());
		}
		else if (number instanceof Integer)
		{
			return new BigDecimal(number.intValue());
		}
		else
		{
			return new BigDecimal(value);
		}
	}
	public BigInteger convertToObject(final String value, final Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		final Number number = parse(value, -Double.MAX_VALUE, Double.MAX_VALUE, locale);

		if (number instanceof BigInteger)
		{
			return (BigInteger)number;
		}
		else if (number instanceof Long)
		{
			return BigInteger.valueOf(number.longValue());
		}
		else if (number instanceof Integer)
		{
			return BigInteger.valueOf(number.intValue());
		}
		else
		{
			return new BigInteger(value);
		}
	}
	public Byte convertToObject(final String value, final Locale locale)
	{
		final Number number = parse(value, Byte.MIN_VALUE, Byte.MAX_VALUE, locale);

		if (number == null)
		{
			return null;
		}

		return number.byteValue();
	}
	public Double convertToObject(final String value, final Locale locale)
	{
		final Number number = parse(value, -Double.MAX_VALUE, Double.MAX_VALUE, locale);
		// Double.MIN is the smallest nonzero positive number, not the largest
		// negative number

		if (number == null)
		{
			return null;
		}

		return number.doubleValue();
	}
	public Float convertToObject(final String value, final Locale locale)
	{
		final Number number = parse(value, -Float.MAX_VALUE, Float.MAX_VALUE, locale);

		if (number == null)
		{
			return null;
		}

		return number.floatValue();
	}
