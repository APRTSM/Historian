	protected UrlInfo parseRequest(Request request)
	{
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
				if (Application.exists())
				{
					Application application = Application.get();

					if (application.getSecuritySettings().getEnforceMounts())
					{
						// we make an excepion if the homepage itself was mounted, see WICKET-1898
						if (!pageClass.equals(application.getHomePage()))
						{
							// WICKET-5094 only enforce mount if page is mounted
							Url reverseUrl = application.getRootRequestMapper().mapHandler(
								new RenderPageRequestHandler(new PageProvider(pageClass)));
							if (!matches(reverseUrl))
							{
								return null;
							}
						}
					}
				}

				// extract the PageParameters from URL if there are any
				PageParameters pageParameters = extractPageParameters(request, 3,
					pageParametersEncoder);

				return new UrlInfo(info, pageClass, pageParameters);
			}
		}
		return null;
	}
