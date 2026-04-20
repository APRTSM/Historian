	public PageParameters decodePageParameters(final Request request)
	{
		PageParameters parameters = new PageParameters();

		int i = 0;
		for (String s : request.getUrl().getSegments())
		{
			parameters.set(i, s);
			++i;
		}

		IRequestParameters requestParameters = request.getRequestParameters();
		for (String paramName : requestParameters.getParameterNames())
		{
			List<StringValue> parameterValues = requestParameters.getParameterValues(paramName);
			for (StringValue paramValue : parameterValues)
			{
				parameters.add(paramName, paramValue);
			}
		}

		return parameters.isEmpty() ? null : parameters;
	}
