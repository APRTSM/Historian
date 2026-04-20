	protected PageComponentInfo getPageComponentInfo(Url url)
	{
		if (url == null)
		{
			throw new IllegalStateException("Argument 'url' may not be null.");
		}
		if (url.getQueryParameters().size() > 0)
		{
			QueryParameter param = url.getQueryParameters().get(0);
			if (Strings.isEmpty(param.getValue()))
			{
				return PageComponentInfo.parse(param.getName());
			}
		}
		return null;
	}
