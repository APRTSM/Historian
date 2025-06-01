	public final Time toTime() throws StringValueConversionException
	{
		try
		{
			return Time.valueOf(text);
		}
		catch (ParseException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text
				+ "' to a Time value", e);
		}
	}
	public final <T> T to(final Class<T> type) throws StringValueConversionException
	{
		if (type == null)
		{
			return null;
		}

		if (type == String.class)
		{
			return (T)toString();
		}

		if ((type == Integer.TYPE) || (type == Integer.class))
		{
			return (T)toInteger();
		}

		if ((type == Long.TYPE) || (type == Long.class))
		{
			return (T)toLongObject();
		}

		if ((type == Boolean.TYPE) || (type == Boolean.class))
		{
			return (T)toBooleanObject();
		}

		if ((type == Double.TYPE) || (type == Double.class))
		{
			return (T)toDoubleObject();
		}

		if ((type == Character.TYPE) || (type == Character.class))
		{
			return (T)toCharacter();
		}

		if (type == Time.class)
		{
			return (T)toTime();
		}

		if (type == Duration.class)
		{
			return (T)toDuration();
		}

		if (type.isEnum())
		{
			return (T)toEnum((Class)type);
		}

		throw new StringValueConversionException("Cannot convert '" + toString() + "'to type "
			+ type);
	}
	public final long toLong(final long defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toLong();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a long: %s", text,
						x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}
	public final char toChar(final char defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toChar();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a character: %s", text,
						x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}
	public final Long toLongObject() throws StringValueConversionException
	{
		try
		{
			return new Long(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text
				+ "' to a Long value", e);
		}
	}
	public boolean equals(final Object obj)
	{
		if (obj instanceof StringValue)
		{
			StringValue stringValue = (StringValue)obj;
			return Objects.isEqual(text, stringValue.text) && locale.equals(stringValue.locale);
		}
		else
		{
			return false;
		}
	}
	public final long toLong() throws StringValueConversionException
	{
		try
		{
			return Long.parseLong(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text
				+ "' to a long value", e);
		}
	}
	public final int toInt() throws StringValueConversionException
	{
		try
		{
			return Integer.parseInt(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text
				+ "' to an int value", e);
		}
	}
	public final Integer toInteger() throws StringValueConversionException
	{
		try
		{
			return new Integer(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text
				+ "' to an Integer value", e);
		}
	}
	public final double toDouble() throws StringValueConversionException
	{
		try
		{
			return NumberFormat.getNumberInstance(locale).parse(text).doubleValue();
		}
		catch (ParseException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text
				+ "' to a double value", e);
		}
	}
	public final Duration toDuration(final Duration defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toDuration();
			}
			catch (Exception x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a Duration: %s", text,
						x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}
	public final double toDouble(final double defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toDouble();
			}
			catch (Exception x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a double: %s", text,
						x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}
	public final Time toTime(final Time defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toTime();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a Time: %s", text,
						x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}
	public final boolean toBoolean(final boolean defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toBoolean();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a boolean: %s", text,
						x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}
	public final int toInt(final int defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toInt();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to an integer: %s", text,
						x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}
