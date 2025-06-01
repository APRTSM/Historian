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
