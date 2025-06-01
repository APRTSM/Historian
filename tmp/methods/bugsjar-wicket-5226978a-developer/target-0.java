	public MockWebApplication(final WebApplication application, final String path)
	{
		this.application = application;

		context = newServletContext(path);

		filter = new WicketFilter()
		{
			@Override
			protected IWebApplicationFactory getApplicationFactory()
			{
				return new IWebApplicationFactory()
				{
					public WebApplication createApplication(WicketFilter filter)
					{
						return application;
					};
				};
			}
		};

		try
		{
			filter.init(new FilterConfig()
			{

				public ServletContext getServletContext()
				{
					return context;
				}

				public Enumeration<?> getInitParameterNames()
				{
					return null;
				}

				public String getInitParameter(String name)
				{
					if (name.equals(WicketFilter.FILTER_MAPPING_PARAM))
					{
						return WicketFilter.SERVLET_PATH_HOLDER;
						// return "/" + MockWebApplication.this.getName() +
						// "/*";
					}
					return null;
				}

				public String getFilterName()
				{
					return "WicketMockServlet";
				}
			});
		}
		catch (ServletException e)
		{
			throw new RuntimeException(e);
		}

		Application.set(this.application);

		// Construct mock session, request and response
		servletSession = new MockHttpSession(context);
		servletSession.setTemporary(initializeHttpSessionAsTemporary());
		servletRequest = new MockHttpServletRequest(this.application, servletSession, context);
		servletResponse = new MockHttpServletResponse(servletRequest)
		{
			@Override
			public void addCookie(Cookie cookie)
			{
				super.addCookie(cookie);
				cookiesOfThisSession.add(cookie);
			}
		};

		// Construct request and response using factories
		wicketRequest = this.application.newWebRequest(servletRequest);
		wicketResponse = this.application.newWebResponse(servletResponse);

		// Create request cycle
		createRequestCycle();

		this.application.getRequestCycleSettings().setRenderStrategy(
			IRequestCycleSettings.ONE_PASS_RENDER);
		// Don't buffer the response, as this can break ajax tests: see WICKET-1264
		this.application.getRequestCycleSettings().setBufferResponse(false);
		if (this.application.getResourceFinder() == null)
		{
			this.application.getResourceSettings().setResourceFinder(
				new WebApplicationPath(context));
		}
		this.application.getPageSettings().setAutomaticMultiWindowSupport(false);

		// Since the purpose of MockWebApplication is singlethreaded
		// programmatic testing it doesn't make much sense to have a
		// modification watcher thread started to watch for changes in the
		// markup.
		// Disabling this also helps test suites with many test cases
		// (problems has been noticed with >~300 test cases). The problem
		// is that even if the wicket tester is GC'ed the modification
		// watcher still runs, taking up file handles and memory, leading
		// to "Too many files opened" or a regular OutOfMemoryException
		this.application.getResourceSettings().setResourcePollFrequency(null);
	}
	public WebRequestCycle setupRequestAndResponse(boolean isAjax)
	{
		servletRequest.initialize();
		servletResponse.initialize();
		servletRequest.addCookies(cookiesOfThisSession);
		servletRequest.setParameters(parametersForNextRequest);
		if (isAjax)
		{
			servletRequest.addHeader("Wicket-Ajax", "Yes");
		}
		parametersForNextRequest.clear();
		wicketRequest = application.newWebRequest(servletRequest);
		wicketResponse = application.newWebResponse(servletResponse);
		WebRequestCycle requestCycle = createRequestCycle();
		if (!initializeHttpSessionAsTemporary())
			application.getSessionStore().bind(wicketRequest, wicketSession);
		wicketResponse.setAjax(wicketRequest.isAjax());
		return requestCycle;
	}
