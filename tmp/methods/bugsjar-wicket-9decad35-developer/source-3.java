	public PageParameters decodePageParameters(final Request request)
	{
		PageParameters parameters = new PageParameters();

		int i = 0;
		for (String s : request.getUrl().getSegments())
		{
			parameters.set(i, s);
			++i;
		}

		for (QueryParameter p : request.getUrl().getQueryParameters())
		{
			parameters.add(p.getName(), p.getValue());
		}

		return parameters.isEmpty() ? null : parameters;
	}
	public Set<String> getParameterNames()
	{
		Set<String> result = new HashSet<String>();
		for (IRequestParameters p : parameters)
		{
			result.addAll(p.getParameterNames());
		}
		return Collections.unmodifiableSet(result);
	}
	public CombinedRequestParametersAdapter(final IRequestParameters... parameters)
	{
		if (parameters == null)
		{
			throw new IllegalStateException("Argument 'parameters' may not be null");
		}
		this.parameters = parameters;
	}
	public Set<String> getParameterNames()
	{
		Set<String> result = new HashSet<String>();
		for (QueryParameter parameter : url.getQueryParameters())
		{
			result.add(parameter.getName());
		}
		return Collections.unmodifiableSet(result);
	}
