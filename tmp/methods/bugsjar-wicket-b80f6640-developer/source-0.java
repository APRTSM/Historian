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
