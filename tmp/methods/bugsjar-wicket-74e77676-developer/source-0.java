	protected UrlInfo parseRequest(Request request)
	{
		if (Application.exists())
		{
			if (Application.get().getSecuritySettings().getEnforceMounts())
			{
				return null;
			}
		}

		Url url = request.getUrl();
		if (matches(url))
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			// load the page class
			String className = url.getSegments().get(2);
			Class<? extends IRequestablePage> pageClass = getPageClass(className);

			if (pageClass != null && IRequestablePage.class.isAssignableFrom(pageClass))
			{

				// extract the PageParameters from URL if there are any
				PageParameters pageParameters = extractPageParameters(request, 3,
					pageParametersEncoder);

				return new UrlInfo(info, pageClass, pageParameters);
			}
		}
		return null;
	}
