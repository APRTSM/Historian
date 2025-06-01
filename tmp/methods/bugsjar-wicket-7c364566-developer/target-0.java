	protected DateTimeFormatter getFormat(Locale locale)
	{
		return DateTimeFormat.forPattern(getDatePattern(locale))
			.withLocale(locale)
			.withPivotYear(2000);
	}
