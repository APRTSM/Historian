	public PageParameters decodePageParameters(final Url url)
	{
		PageParameters parameters = new PageParameters();

		int i = 0;
		for (String s : url.getSegments())
		{
			parameters.set(i, s);
			++i;
		}

		for (QueryParameter p : url.getQueryParameters())
		{
			String parameterName = p.getName();
			if (Strings.isEmpty(parameterName) == false)
			{
				parameters.add(parameterName, p.getValue(), INamedParameters.Type.QUERY_STRING);
			}
		}

		return parameters.isEmpty() ? null : parameters;
	}
