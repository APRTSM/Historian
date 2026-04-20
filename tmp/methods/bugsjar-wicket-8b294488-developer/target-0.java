	public final <C> IConverter<C> get(Class<C> c)
	{
		@SuppressWarnings("unchecked")
		IConverter<C> converter = (IConverter<C>)classToConverter.get(c.getName());

		if (converter == null)
		{
			// Date based converters work with thread unsafe DateFormats and
			// a new instance should be created for each usage
			if (Date.class.equals(c))
			{
				converter = (IConverter<C>) new DateConverter();
			}
			else if  (java.sql.Date.class.equals(c))
			{
				converter = (IConverter<C>) new SqlDateConverter();
			}
			else if (java.sql.Time.class.equals(c))
			{
				converter = (IConverter<C>) new SqlTimeConverter();
			}
			else if (java.sql.Timestamp.class.equals(c))
			{
				converter = (IConverter<C>) new SqlTimestampConverter();
			}
			else if (Calendar.class.equals(c))
			{
				converter = (IConverter<C>) new CalendarConverter();
			}
		}
		return converter;
	}
