	public String encodeURL(CharSequence url)
	{
		Args.notNull(url, "url");

		/*
		  WICKET-4645 - always pass absolute url to the web container for encoding
		  because when REDIRECT_TO_BUFFER is in use Wicket may render PageB when
		  PageA is actually the requested one and the web container cannot resolve
		  the base url properly
		 */
		UrlRenderer urlRenderer = RequestCycle.get().getUrlRenderer();
		Url relativeUrl = Url.parse(url);
		String fullUrl = urlRenderer.renderFullUrl(relativeUrl);
		String encodedFullUrl = httpServletResponse.encodeURL(fullUrl);
		final String encodedRelativeUrl;
		if (fullUrl.equals(encodedFullUrl))
		{
			// no encoding happened so just reuse the relative url
			encodedRelativeUrl = url.toString();
		}
		else
		{
			// get the relative url with the jsessionid encoded in it
			Url _encoded = Url.parse(encodedFullUrl);
			encodedRelativeUrl = urlRenderer.renderRelativeUrl(_encoded);
		}
		return encodedRelativeUrl;
	}
	public String encodeRedirectURL(CharSequence url)
	{
		Args.notNull(url, "url");

		/*
		  WICKET-4854 - always pass absolute url to the web container for encoding
		  because when REDIRECT_TO_BUFFER is in use Wicket may render PageB when
		  PageA is actually the requested one and the web container cannot resolve
		  the base url properly
		 */
		UrlRenderer urlRenderer = new UrlRenderer(webRequest);
		Url relativeUrl = Url.parse(url);
		String fullUrl = urlRenderer.renderFullUrl(relativeUrl);
		String encodedFullUrl = httpServletResponse.encodeRedirectURL(fullUrl);
		final String encodedRelativeUrl;
		if (fullUrl.equals(encodedFullUrl))
		{
			// no encoding happened so just reuse the relative url
			encodedRelativeUrl = url.toString();
		}
		else
		{
			// get the relative url with the jsessionid encoded in it
			Url _encoded = Url.parse(encodedFullUrl);
			encodedRelativeUrl = urlRenderer.renderRelativeUrl(_encoded);
		}
		return encodedRelativeUrl;
	}
