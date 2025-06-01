	public PageParameters(final String keyValuePairs, final String delimiter)
	{
		super();

		setOnRequestCycle();

		// We can not use ValueMaps constructor as it uses
		// VariableAssignmentParser which is more suitable for markup
		// attributes, rather than URL parameters. URL param keys for
		// examples are allowed to start with a digit (e.g. 0=xxx)
		// and quotes are not "quotes".

		// Get list of strings separated by the delimiter
		final StringList pairs = StringList.tokenize(keyValuePairs, delimiter);

		// Go through each string in the list
		for (IStringIterator iterator = pairs.iterator(); iterator.hasNext();)
		{
			// Get the next key value pair
			final String pair = iterator.next();

			final int pos = pair.indexOf('=');
			if (pos == 0)
			{
				throw new IllegalArgumentException("URL parameter is missing the lvalue: " + pair);
			}
			else if (pos != -1)
			{
				final String key = pair.substring(0, pos).trim();
				final String value = pair.substring(pos + 1).trim();

				put(key, value);
			}
			else
			{
				final String key = pair.trim();
				final String value = null;

				put(key, value);
			}
		}
	}
	public final void setParameters(final Map<?, ?> parameters)
	{
		if (parameters == null)
		{
			Resource.parameters.set(null);
		}
		else
		{
			Resource.parameters.set(new ValueMap(parameters));
		}
	}
	public Time getAsTime(String key)
	{
		return getAsTime(key, null);
	}
	public ValueMap(final Map map)
	{
		super();

		super.putAll(map);
	}
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		for (final Iterator iterator = entrySet().iterator(); iterator.hasNext();)
		{
			final Map.Entry entry = (Map.Entry)iterator.next();
			buffer.append(entry.getKey());
			buffer.append(" = \"");
			final Object value = entry.getValue();
			if (value == null)
			{
				buffer.append("null");
			}
			else if (value.getClass().isArray())
			{
				buffer.append(Arrays.asList((Object[])value));
			}
			else
			{
				buffer.append(value);
			}

			buffer.append('\"');
			if (iterator.hasNext())
			{
				buffer.append(' ');
			}
		}
		return buffer.toString();
	}
	public Duration getAsDuration(String key)
	{
		return getAsDuration(key, null);
	}
	public Boolean getAsBoolean(String key)
	{
		if (!containsKey(key))
			return null;

		try
		{
			return getBoolean(key);
		}
		catch (StringValueConversionException ignored)
		{
			return null;
		}
	}
	public Duration getAsDuration(String key, Duration defaultValue)
	{
		if (!containsKey(key))
			return defaultValue;

		try
		{
			return getDuration(key);
		}
		catch (StringValueConversionException ignored)
		{
			return defaultValue;
		}
	}
	private <T extends Enum<T>> T getEnumImpl(String key, Class<?> eClass, T defaultValue)
	{
		if (eClass == null)
			throw new IllegalArgumentException("eClass value cannot be null");

		String value = getString(key);
		if (value == null)
			return defaultValue;

		Method valueOf = null;
		try
		{
			valueOf = eClass.getMethod("valueOf", String.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException("Could not find method valueOf(String s) for " +
				eClass.getName(), e);
		}

		try
		{
			return (T)valueOf.invoke(eClass, value);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException("Could not invoke method valueOf(String s) on " +
				eClass.getName(), e);
		}
		catch (InvocationTargetException e)
		{
			// IllegalArgumentException thrown if enum isn't defined - just return default
			if (e.getCause() instanceof IllegalArgumentException)
			{
				return defaultValue;
			}
			throw new RuntimeException(e); // shouldn't happen
		}
	}
	public Double getAsDouble(String key)
	{
		if (!containsKey(key))
			return null;

		try
		{
			return getDouble(key);
		}
		catch (StringValueConversionException ignored)
		{
			return null;
		}
	}
	public <T extends Enum<T>> T getAsEnum(String key, Class<T> eClass, T defaultValue)
	{
		return getEnumImpl(key, eClass, defaultValue);
	}
	public ValueMap(final String keyValuePairs, final String delimiter)
	{
		super();

		int start = 0;
		int equalsIndex = keyValuePairs.indexOf('=');
		int delimiterIndex = keyValuePairs.indexOf(delimiter, equalsIndex);
		if (delimiterIndex == -1)
		{
			delimiterIndex = keyValuePairs.length();
		}
		while (equalsIndex != -1)
		{
			if (delimiterIndex < keyValuePairs.length())
			{
				int equalsIndex2 = keyValuePairs.indexOf('=', delimiterIndex + 1);
				if (equalsIndex2 != -1)
				{
					delimiterIndex = keyValuePairs.lastIndexOf(delimiter, equalsIndex2);
				}
				else
				{
					delimiterIndex = keyValuePairs.length();
				}
			}
			String key = keyValuePairs.substring(start, equalsIndex);
			String value = keyValuePairs.substring(equalsIndex + 1, delimiterIndex);
			put(key, value);
			if (delimiterIndex < keyValuePairs.length())
			{
				start = delimiterIndex + 1;
				equalsIndex = keyValuePairs.indexOf('=', start);
				if (equalsIndex != -1)
				{
					delimiterIndex = keyValuePairs.indexOf(delimiter, equalsIndex);
					if (delimiterIndex == -1)
					{
						delimiterIndex = keyValuePairs.length();
					}
				}
			}
			else
			{
				equalsIndex = -1;
			}
		}
	}
	public Time getAsTime(String key, Time defaultValue)
	{
		if (!containsKey(key))
			return defaultValue;

		try
		{
			return getTime(key);
		}
		catch (StringValueConversionException ignored)
		{
			return defaultValue;
		}
	}
	public Long getAsLong(String key)
	{
		if (!containsKey(key))
			return null;

		try
		{
			return getLong(key);
		}
		catch (StringValueConversionException ignored)
		{
			return null;
		}
	}
	public double getAsDouble(String key, double defaultValue)
	{
		try
		{
			return getDouble(key, defaultValue);
		}
		catch (StringValueConversionException ignored)
		{
			return defaultValue;
		}
	}
	public <T extends Enum<T>> T getAsEnum(String key, T defaultValue)
	{
		if (defaultValue == null)
			throw new IllegalArgumentException("Default value cannot be null");
		return getEnumImpl(key, defaultValue.getClass(), defaultValue);
	}
	public void putAll(final Map map)
	{
		checkMutability();
		super.putAll(map);
	}
	public <T extends Enum<T>> T getAsEnum(String key, Class<T> eClass)
	{
		return getEnumImpl(key, eClass, null);
	}
	public Integer getAsInteger(String key)
	{
		if (!containsKey(key))
			return null;

		try
		{
			return getInt(key);
		}
		catch (StringValueConversionException ignored)
		{
			return null;
		}
	}
