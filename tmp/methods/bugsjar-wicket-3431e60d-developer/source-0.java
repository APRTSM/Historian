	protected CharSequence encode(RequestCycle requestCycle,
			IBookmarkablePageRequestTarget requestTarget)
	{
		// Begin encoding URL
		final AppendingStringBuffer url = new AppendingStringBuffer(64);
		url.append(urlPrefix(requestCycle));

		// Get page Class
		final Class pageClass = requestTarget.getPageClass();
		final Application application = Application.get();

		// Find pagemap name
		String pageMapName = requestTarget.getPageMapName();
		if (pageMapName == null)
		{
			IRequestTarget currentTarget = requestCycle.getRequestTarget();
			if (currentTarget instanceof IPageRequestTarget)
			{
				Page currentPage = ((IPageRequestTarget)currentTarget).getPage();
				final PageMap pageMap = currentPage.getPageMap();
				if (pageMap.isDefault())
				{
					pageMapName = "";
				}
				else
				{
					pageMapName = pageMap.getName();
				}
			}
			else
			{
				pageMapName = "";
			}
		}

		boolean firstParameter = true;
		if (!application.getHomePage().equals(pageClass) || !"".equals(pageMapName)
				|| requestTarget instanceof BookmarkableListenerInterfaceRequestTarget)
		{
			firstParameter = false;
			url.append('?');
			url.append(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME);
			url.append('=');


			// Add <page-map-name>:<bookmarkable-page-class>
			url.append(pageMapName + Component.PATH_SEPARATOR + pageClass.getName());
		}

		// Is it a bookmarkable interface listener?
		if (requestTarget instanceof BookmarkableListenerInterfaceRequestTarget)
		{
			BookmarkableListenerInterfaceRequestTarget listenerTarget = (BookmarkableListenerInterfaceRequestTarget)requestTarget;
			if (firstParameter == true)
			{
				url.append("?");
			}
			else
			{
				url.append("&");
			}
			firstParameter = false;
			url.append(INTERFACE_PARAMETER_NAME);
			url.append("=");
			url.append(Component.PATH_SEPARATOR);
			url.append(listenerTarget.getComponentPath());
			url.append(Component.PATH_SEPARATOR);
			url.append(Component.PATH_SEPARATOR);
			url.append(listenerTarget.getInterfaceName());
		}

		// Get page parameters
		final PageParameters parameters = requestTarget.getPageParameters();
		if (parameters != null)
		{
			for (Object element : parameters.keySet())
			{
				final String key = (String)element;
				final String value = parameters.getString(key);
				if (value != null)
				{
					String escapedValue = value;
					try
					{
						escapedValue = URLEncoder.encode(escapedValue, application
								.getRequestCycleSettings().getResponseRequestEncoding());
					}
					catch (UnsupportedEncodingException ex)
					{
						log.error(ex.getMessage(), ex);
					}
					if (!firstParameter)
					{
						url.append('&');
					}
					else
					{
						firstParameter = false;
						url.append('?');
					}
					url.append(key);
					url.append('=');
					url.append(escapedValue);
				}
			}
		}
		return requestCycle.getOriginalResponse().encodeURL(url);
	}
