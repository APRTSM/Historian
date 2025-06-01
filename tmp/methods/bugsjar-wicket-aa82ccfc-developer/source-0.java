	protected UrlInfo parseRequest(Request request)
	{
		if (matches(request))
		{
			Url url = request.getUrl();

			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			List<String> segments = url.getSegments();

			// load the page class
			String className;
			if (segments.size() >= 3)
			{
				className = segments.get(2);
			}
			else
			{
				className = segments.get(1);
			}

			Class<? extends IRequestablePage> pageClass = getPageClass(className);

			if (pageClass != null && IRequestablePage.class.isAssignableFrom(pageClass))
			{
				if (Application.exists())
				{
					Application application = Application.get();

					if (application.getSecuritySettings().getEnforceMounts())
					{
						// we make an exception if the homepage itself was mounted, see WICKET-1898
						if (!pageClass.equals(application.getHomePage()))
						{
							// WICKET-5094 only enforce mount if page is mounted
							Url reverseUrl = application.getRootRequestMapper().mapHandler(
								new RenderPageRequestHandler(new PageProvider(pageClass)));
							if (!matches(request.cloneWithUrl(reverseUrl)))
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
