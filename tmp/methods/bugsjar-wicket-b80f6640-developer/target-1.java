	protected NumberFormat newNumberFormat(Locale locale)
	{
		NumberFormat numberFormat  = NumberFormat.getIntegerInstance(locale);
		numberFormat.setParseIntegerOnly(true);
		numberFormat.setGroupingUsed(false);
		return numberFormat;
	}
