	protected ValueMap decodeParameters(String urlFragment, Map<String,Object> urlParameters)
	{
		// Hack off any leading slash
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}
		// Hack off any trailing slash
		if (urlFragment.length() > 0 && urlFragment.endsWith("/"))
		{
			urlFragment = urlFragment.substring(0, urlFragment.length() - 1);
		}

		if (urlFragment.length() == 0)
		{
			return new ValueMap(urlParameters != null ? urlParameters : Collections.EMPTY_MAP);
		}

		// Split into pairs
		final String[] pairs = urlFragment.split("/");

		// If we don't have an even number of pairs
		if (pairs.length % 2 != 0)
		{
			log.warn("URL fragment has unmatched key/value pairs, responding with 404. Fragment: " +
				urlFragment);
			throw new AbortWithWebErrorCodeException(404);
		}

		// Loop through pairs

		ValueMap parameters = new ValueMap();
		for (int i = 0; i < pairs.length; i += 2)
		{
			String value = pairs[i + 1];
			value = urlDecodePathComponent(value);
			parameters.add(pairs[i], value);
		}


		if (urlParameters != null)
		{
			parameters.putAll(urlParameters);
		}

		return parameters;
	}
	protected void appendParameters(AppendingStringBuffer url, Map<?,?> parameters)
	{
		if (parameters != null && parameters.size() > 0)
		{
			for (Entry<?, ?> entry1 : parameters.entrySet())
			{
				Object value = ((Entry<?, ?>) entry1).getValue();
				if (value != null)
				{
					if (value instanceof String[])
					{
						String[] values = (String[]) value;
						for (String value1 : values)
						{
							appendValue(url, ((Entry<?, ?>) entry1).getKey().toString(), value1);
						}
					} else
					{
						appendValue(url, ((Entry<?, ?>) entry1).getKey().toString(), value.toString());
					}
				}
			}
		}
	}
	private void appendValue(AppendingStringBuffer url, String key, String value)
	{
		String escapedValue = urlEncodePathComponent(value);
		if (!Strings.isEmpty(escapedValue))
		{
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(key).append("/").append(escapedValue).append("/");
		}
	}
