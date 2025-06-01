	public final Boolean toOptionalBoolean() throws StringValueConversionException
	{
		return (text == null) ? null : toBooleanObject();
	}
	public final Double toOptionalDouble() throws StringValueConversionException
	{
		return (text == null) ? null : toDoubleObject();
	}
	public final Character toOptionalCharacter() throws StringValueConversionException
	{
		return (text == null) ? null : toCharacter();
	}
	public final Time toOptionalTime() throws StringValueConversionException
	{
		return (text == null) ? null : toTime();
	}
	public final Integer toOptionalInteger() throws StringValueConversionException
	{
		return (text == null) ? null : toInteger();
	}
	public final Duration toOptionalDuration() throws StringValueConversionException
	{
		return (text == null) ? null : toDuration();
	}
	public final Long toOptionalLong() throws StringValueConversionException
	{
		return (text == null) ? null : toLongObject();
	}
