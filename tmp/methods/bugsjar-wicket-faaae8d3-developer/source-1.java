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
	private boolean matches(final Request request)
	{
		boolean matches = false;
		Url url = request.getUrl();
		String namespace = getContext().getNamespace();
		String pageIdentifier = getContext().getPageIdentifier();
		if (urlStartsWith(url, namespace, pageIdentifier))
		{
			matches = true;
		}
		else if (urlStartsWith(request.getClientUrl(), namespace, pageIdentifier) && urlStartsWith(url, pageIdentifier))
		{
			matches = true;
		}
		else if (urlStartsWith(request.getClientUrl(), pageIdentifier) && urlStartsWith(url, pageIdentifier))
		{
			matches = true;
		}

		return matches;
	}
