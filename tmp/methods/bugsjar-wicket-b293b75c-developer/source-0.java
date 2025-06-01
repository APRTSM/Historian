	public IRequestHandler mapRequest(Request request)
	{
		final Url url = request.getUrl();

		if (url.getSegments().size() == 0)
		{
			final Class<? extends IRequestablePage> homePageClass = getContext().getHomePageClass();

			final PageProvider pageProvider;

			if (url.getQueryParameters().size() > 0)
			{
				PageParameters pageParameters = extractPageParameters(request, 0,
					pageParametersEncoder);
				pageProvider = new PageProvider(homePageClass, pageParameters);
			}
			else
			{
				pageProvider = new PageProvider(homePageClass);
			}

			return new RenderPageRequestHandler(pageProvider);
		}
		else
		{
			return null;
		}
	}
