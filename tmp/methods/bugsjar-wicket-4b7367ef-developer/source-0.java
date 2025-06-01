	public RedirectRequestHandler(final String redirectUrl, final int status)
	{
		if ((status != HttpServletResponse.SC_MOVED_PERMANENTLY) &&
			(status != HttpServletResponse.SC_MOVED_TEMPORARILY))
		{
			throw new IllegalStateException("Status must be either 301 or 302, but was: " + status);
		}
		this.redirectUrl = redirectUrl;
		this.status = status;
	}
	public void respond(final IRequestCycle requestCycle)
	{
		final String location;

		if (redirectUrl.startsWith("/"))
		{
			// context-absolute url
			location = requestCycle.getUrlRenderer().renderContextRelativeUrl(redirectUrl);
		}
		else
		{
			// if relative url, servlet container will translate to absolute as
			// per the servlet spec
			// if absolute url still do the same
			location = redirectUrl;
		}

		WebResponse response = (WebResponse)requestCycle.getResponse();

		if (status == HttpServletResponse.SC_MOVED_TEMPORARILY)
		{
			response.sendRedirect(location);
		}
		else
		{
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			response.setHeader("Location", location);
		}
	}
