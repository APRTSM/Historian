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
