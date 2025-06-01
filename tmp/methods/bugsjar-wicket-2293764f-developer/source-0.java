	public ServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix, Url url)
	{
		Args.notNull(httpServletRequest, "httpServletRequest");
		Args.notNull(filterPrefix, "filterPrefix");

		this.httpServletRequest = httpServletRequest;

		errorAttributes = ErrorAttributes.of(httpServletRequest, filterPrefix);

		forwardAttributes = ForwardAttributes.of(httpServletRequest, filterPrefix);

		if (forwardAttributes != null || errorAttributes != null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Setting filterPrefix('{}') to '' because there is either an error or a forward. {}, {}",
						new Object[] {filterPrefix, forwardAttributes, errorAttributes});
			}
			// the filter prefix is not needed when the current request is internal
			// see WICKET-4387
			this.filterPrefix = "";
		} else
		{
			this.filterPrefix = filterPrefix;
		}

		if (url != null)
		{
			this.url = url;
		}
		else
		{
			this.url = getContextRelativeUrl(httpServletRequest.getRequestURI(), filterPrefix);
		}
	}
