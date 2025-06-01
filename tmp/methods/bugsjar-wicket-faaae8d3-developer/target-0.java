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
		// baseUrl = 'bookmarkable/com.example.SomePage', requestUrl = 'bookmarkable/com.example.SomePage'
		else if (baseUrl.getSegments().size() == 2 && urlStartsWith(baseUrl, bookmarkableIdentifier) && url.getSegments().size() == 2 && urlStartsWith(url, bookmarkableIdentifier))
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
