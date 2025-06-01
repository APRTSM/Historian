	protected DateTimeFormatter getFormat(Locale locale)
	{
		return DateTimeFormat.forPattern(getDatePattern(locale)).withPivotYear(2000);
	}
