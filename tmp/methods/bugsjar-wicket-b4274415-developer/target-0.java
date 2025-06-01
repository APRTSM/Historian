	public final Boolean toOptionalBoolean() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toBooleanObject();
	}
	public final Double toOptionalDouble() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toDoubleObject();
	}
	public final Character toOptionalCharacter() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toCharacter();
	}
	public final Time toOptionalTime() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toTime();
	}
	public final Integer toOptionalInteger() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toInteger();
	}
	public final Duration toOptionalDuration() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toDuration();
	}
	public final Long toOptionalLong() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toLongObject();
	}
