	private boolean matches(final Request request)
	{
		boolean matches = false;
		Url url = request.getUrl();
		Url baseUrl = request.getClientUrl();
		String namespace = getContext().getNamespace();
		String bookmarkableIdentifier = getContext().getBookmarkableIdentifier();
		String pageIdentifier = getContext().getPageIdentifier();

		if (url.getSegments().size() >= 3 && urlStartsWith(url, namespace, bookmarkableIdentifier))
		{
			matches = true;
		}
		// baseUrl = 'wicket/bookmarkable/com.example.SomePage[?...]', requestUrl = 'bookmarkable/com.example.SomePage'
		else if (baseUrl.getSegments().size() == 3 && urlStartsWith(baseUrl, namespace, bookmarkableIdentifier) && url.getSegments().size() >= 2 && urlStartsWith(url, bookmarkableIdentifier))
		{
			matches = true;
		}
		// baseUrl = 'wicket/page[?...]', requestUrl = 'bookmarkable/com.example.SomePage'
		else if (baseUrl.getSegments().size() == 2 && urlStartsWith(baseUrl, namespace, pageIdentifier) && url.getSegments().size() >= 2 && urlStartsWith(url, bookmarkableIdentifier))
		{
			matches = true;
		}

		return matches;
	}
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
	public int getCompatibilityScore(Request request)
	{
		int score = 0;
		if (matches(request))
		{
			score = Integer.MAX_VALUE;
		}
		return score;
	}
