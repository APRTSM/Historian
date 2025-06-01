	public RedirectRequestHandler(final String redirectUrl, final int status)
	{
		if ((status != HttpServletResponse.SC_MOVED_PERMANENTLY) &&
			(status != HttpServletResponse.SC_MOVED_TEMPORARILY) &&
			(status != HttpServletResponse.SC_SEE_OTHER))
		{
			throw new IllegalStateException("Status must be either 301, 302 or 303, but was: " + status);
		}
		this.redirectUrl = redirectUrl;
		this.status = status;
	}
	public void respond(final IRequestCycle requestCycle)
	{
		final String location;

		final String url = getRedirectUrl();

		if (url.charAt(0) == '/')
		{
			// context-absolute url
			location = requestCycle.getUrlRenderer().renderContextRelativeUrl(url);
		}
		else
		{
			// if relative url, servlet container will translate to absolute as
			// per the servlet spec
			// if absolute url still do the same
			location = url;
		}

		WebResponse response = (WebResponse)requestCycle.getResponse();

		if (status == HttpServletResponse.SC_MOVED_TEMPORARILY)
		{
			response.sendRedirect(location);
		}
		else
		{
			response.setStatus(status);
			response.setHeader("Location", location);
		}
	}
