	boolean processRequest(ServletRequest request, final ServletResponse response,
		final FilterChain chain) throws IOException, ServletException
	{
		final ThreadContext previousThreadContext = ThreadContext.detach();

		// Assume we are able to handle the request
		boolean res = true;

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();

		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
			HttpServletResponse httpServletResponse = (HttpServletResponse)response;

			// Make sure getFilterPath() gets called before checkIfRedirectRequired()
			String filterPath = getFilterPath(httpServletRequest);

			if (filterPath == null)
			{
				throw new IllegalStateException("filter path was not configured");
			}

			if (shouldIgnorePath(httpServletRequest))
			{
				log.debug("Ignoring request {}", httpServletRequest.getRequestURL());
				if (chain != null)
				{
					chain.doFilter(request, response);
				}
				return false;
			}

			if ("OPTIONS".equals(httpServletRequest.getMethod()))
			{
				// handle the OPTIONS request outside of normal request processing.
				// wicket pages normally only support GET and POST methods, but resources and
				// special pages acting like REST clients can also support other methods, so
				// we include them all.
				httpServletResponse.setStatus(HttpServletResponse.SC_OK);
				httpServletResponse.setHeader("Allow",
					"GET,POST,OPTIONS,PUT,HEAD,PATCH,DELETE,TRACE");
				httpServletResponse.setHeader("Content-Length", "0");
				return true;
			}

			String redirectURL = checkIfRedirectRequired(httpServletRequest);
			if (redirectURL == null)
			{
				// No redirect; process the request
				ThreadContext.setApplication(application);

				WebRequest webRequest = application.createWebRequest(httpServletRequest, filterPath);
				WebResponse webResponse = application.createWebResponse(webRequest,
					httpServletResponse);

				RequestCycle requestCycle = application.createRequestCycle(webRequest, webResponse);
				res = processRequestCycle(requestCycle, webResponse, httpServletRequest, httpServletResponse, chain);
			}
			else
			{
				if (Strings.isEmpty(httpServletRequest.getQueryString()) == false)
				{
					redirectURL += "?" + httpServletRequest.getQueryString();
				}

				try
				{
					// send redirect - this will discard POST parameters if the request is POST
					// - still better than getting an error because of lacking trailing slash
					httpServletResponse.sendRedirect(httpServletResponse.encodeRedirectURL(redirectURL));
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		finally
		{
			ThreadContext.restore(previousThreadContext);

			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}

			if (response.isCommitted())
			{
				response.flushBuffer();
			}
		}
		return res;
	}
	protected boolean processRequestCycle(RequestCycle requestCycle, WebResponse webResponse,
	    HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
		final FilterChain chain) throws IOException, ServletException {
		// Assume we are able to handle the request
		boolean res = true;

		if (!requestCycle.processRequestAndDetach())
		{
			if (chain != null)
			{
				chain.doFilter(httpServletRequest, httpServletResponse);
			}
			res = false;
		}
		else
		{
			webResponse.flush();
		}
		return res;
	}
	public final void setFilterPath(String filterPath)
	{
		// see https://issues.apache.org/jira/browse/WICKET-701
		if (this.filterPath != null)
		{
			throw new IllegalStateException(
				"Filter path is write-once. You can not change it. Current value='" + filterPath + '\'');
		}
		if (filterPath != null)
		{
			filterPath = canonicaliseFilterPath(filterPath);

			// We only need to determine it once. It'll not change.
			if (filterPath.endsWith("/"))
			{
				filterPathLength = filterPath.length() - 1;
			}
			else
			{
				filterPathLength = filterPath.length();
			}
		}
		this.filterPath = filterPath;
	}
