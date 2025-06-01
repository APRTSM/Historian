	public ServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix, Url url)
	{
		Args.notNull(httpServletRequest, "httpServletRequest");
		Args.notNull(filterPrefix, "filterPrefix");

		this.httpServletRequest = httpServletRequest;

		errorAttributes = ErrorAttributes.of(httpServletRequest, filterPrefix);

		forwardAttributes = ForwardAttributes.of(httpServletRequest, filterPrefix);

		this.filterPrefix = filterPrefix;

		if (url != null)
		{
			this.url = url;
		}
		else
		{
			this.url = getContextRelativeUrl(httpServletRequest.getRequestURI(), filterPrefix);
		}
	}
