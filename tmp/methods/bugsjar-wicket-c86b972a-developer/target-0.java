	protected PageComponentInfo getPageComponentInfo(final Url url)
	{
		if (url == null)
		{
			throw new IllegalStateException("Argument 'url' may not be null.");
		}
		else
		{
			for (QueryParameter queryParameter : url.getQueryParameters())
			{
				if (Strings.isEmpty(queryParameter.getValue()))
				{
					PageComponentInfo pageComponentInfo = PageComponentInfo.parse(queryParameter.getName());
					if (pageComponentInfo != null)
					{
						return pageComponentInfo;
					}
				}
			}
		}
		return null;
	}
