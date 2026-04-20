	public String encodeRedirectURL(CharSequence url)
	{
		Args.notNull(url, "url");

		UrlRenderer urlRenderer = RequestCycle.get().getUrlRenderer();

		Url originalUrl = Url.parse(url);

		/*
		 * WICKET-4645 - always pass absolute url to the web container for encoding because when
		 * REDIRECT_TO_BUFFER is in use Wicket may render PageB when PageA is actually the requested
		 * one and the web container cannot resolve the base url properly
		 */
		String fullUrl = urlRenderer.renderFullUrl(originalUrl);
		String encodedFullUrl = httpServletResponse.encodeRedirectURL(fullUrl);

		final String encodedUrl;
		if (originalUrl.isFull())
		{
			encodedUrl = encodedFullUrl;
		}
		else
		{
			if (fullUrl.equals(encodedFullUrl))
			{
				// no encoding happened so just reuse the original url
				encodedUrl = url.toString();
			}
			else
			{
				// get the relative url with the jsessionid encoded in it
				Url _encoded = Url.parse(encodedFullUrl);
				encodedUrl = urlRenderer.renderRelativeUrl(_encoded);
			}
		}
		return encodedUrl;
	}
	public String encodeURL(CharSequence url)
	{
		Args.notNull(url, "url");

		UrlRenderer urlRenderer = RequestCycle.get().getUrlRenderer();

		Url originalUrl = Url.parse(url);

		/*
		  WICKET-4645 - always pass absolute url to the web container for encoding
		  because when REDIRECT_TO_BUFFER is in use Wicket may render PageB when
		  PageA is actually the requested one and the web container cannot resolve
		  the base url properly
		 */
		String fullUrl = urlRenderer.renderFullUrl(originalUrl);
		String encodedFullUrl = httpServletResponse.encodeURL(fullUrl);

		final String encodedUrl;
		if (originalUrl.isFull())
		{
			encodedUrl = encodedFullUrl;
		}
		else
		{
			if (fullUrl.equals(encodedFullUrl))
			{
				// no encoding happened so just reuse the original url
				encodedUrl = url.toString();
			}
			else
			{
				// get the relative url with the jsessionid encoded in it
				Url _encoded = Url.parse(encodedFullUrl);
				encodedUrl = urlRenderer.renderRelativeUrl(_encoded);
			}
		}
		return encodedUrl;
	}
