	private IRequestHandler internalMap(Exception e)
	{
		final Application application = Application.get();

		// check if we are processing an Ajax request and if we want to invoke the failure handler
		if (isProcessingAjaxRequest())
		{
			switch (application.getExceptionSettings().getAjaxErrorHandlingStrategy())
			{
				case INVOKE_FAILURE_HANDLER :
					return new ErrorCodeRequestHandler(500);
			}
		}

		if (e instanceof StalePageException)
		{
			// If the page was stale, just re-render it
			// (the url should always be updated by an redirect in that case)
			return new RenderPageRequestHandler(new PageProvider(((StalePageException)e).getPage()));
		}
		else if (e instanceof PageExpiredException)
		{
			return createPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getPageExpiredErrorPage()));
		}
		else if (e instanceof AuthorizationException ||
			e instanceof ListenerInvocationNotAllowedException)
		{
			return createPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getAccessDeniedPage()));
		}
		else if (e instanceof ResponseIOException)
		{
			logger.error("Connection lost, give up responding.", e);
			return new EmptyRequestHandler();
		}
		else
		{

			final UnexpectedExceptionDisplay unexpectedExceptionDisplay = application.getExceptionSettings()
				.getUnexpectedExceptionDisplay();

			logger.error("Unexpected error occurred", e);

			if (IExceptionSettings.SHOW_EXCEPTION_PAGE.equals(unexpectedExceptionDisplay))
			{
				Page currentPage = extractCurrentPage();
				return createPageRequestHandler(new PageProvider(new ExceptionErrorPage(e,
					currentPage)));
			}
			else if (IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE.equals(unexpectedExceptionDisplay))
			{
				return createPageRequestHandler(new PageProvider(
					application.getApplicationSettings().getInternalErrorPage()));
			}
			else
			{
				// IExceptionSettings.SHOW_NO_EXCEPTION_PAGE
				return new ErrorCodeRequestHandler(500);
			}
		}
	}
	private boolean isProcessingAjaxRequest()
	{
		RequestCycle rc = RequestCycle.get();
		Request request = rc.getRequest();
		if (request instanceof WebRequest)
		{
			return ((WebRequest)request).isAjax();
		}
		return false;
	}
	public IRequestHandler map(Exception e)
	{
		try
		{
			Response response = RequestCycle.get().getResponse();
			if (response instanceof WebResponse)
			{
				// we don't want to cache an exceptional reply in the browser
				((WebResponse)response).disableCaching();
			}
			return internalMap(e);
		}
		catch (RuntimeException e2)
		{
			if (logger.isDebugEnabled())
			{
				logger.error(
					"An error occurred while handling a previous error: " + e2.getMessage(), e2);
			}

			// hmmm, we were already handling an exception! give up
			logger.error("unexpected exception when handling another exception: " + e.getMessage(),
				e);
			return new ErrorCodeRequestHandler(500);
		}
	}
	public final boolean isPageStateless()
	{
		if (isBookmarkable() == false)
		{
			stateless = Boolean.FALSE;
			if (getStatelessHint())
			{
				log.warn("Page '" + this + "' is not stateless because it is not bookmarkable, " +
					"but the stateless hint is set to true!");
			}
		}

		if (getStatelessHint() == false)
		{
			return false;
		}

		if (stateless == null)
		{
			internalInitialize();

			if (isStateless() == false)
			{
				stateless = Boolean.FALSE;
			}
		}

		if (stateless == null)
		{
			Component statefulComponent = visitChildren(Component.class,
				new IVisitor<Component, Component>()
				{
					@Override
					public void component(final Component component, final IVisit<Component> visit)
					{
						if (!component.isStateless())
						{
							visit.stop(component);
						}
					}
				});

			stateless = statefulComponent == null;

			if (log.isDebugEnabled() && !stateless.booleanValue() && getStatelessHint())
			{
				log.debug("Page '{}' is not stateless because of component with path '{}'.", this,
					statefulComponent.getPageRelativePath());
			}

		}

		return stateless;
	}
	public void touchPage(IManageablePage page)
	{
		getRequestAdapter().touch(page);
	}
	public void init(final boolean isServlet, final FilterConfig filterConfig)
		throws ServletException
	{
		this.filterConfig = filterConfig;
		this.isServlet = isServlet;
		initIgnorePaths(filterConfig);

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();
		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			// locate application instance unless it was already specified during construction
			if (application == null)
			{
				applicationFactory = getApplicationFactory();
				application = applicationFactory.createApplication(this);
			}

			application.setName(filterConfig.getFilterName());
			application.setWicketFilter(this);

			// Allow the filterPath to be preset via setFilterPath()
			String configureFilterPath = getFilterPath();

			if (configureFilterPath == null)
			{
				configureFilterPath = getFilterPathFromConfig(filterConfig);

				if (configureFilterPath == null)
				{
					configureFilterPath = getFilterPathFromWebXml(isServlet, filterConfig);

					if (configureFilterPath == null)
					{
						configureFilterPath = getFilterPathFromAnnotation(isServlet);
					}
				}

				if (configureFilterPath != null)
				{
					setFilterPath(configureFilterPath);
				}
			}

			if (getFilterPath() == null)
			{
				log.warn("Unable to determine filter path from filter init-param, web.xml, "
					+ "or servlet 3.0 annotations. Assuming user will set filter path "
					+ "manually by calling setFilterPath(String)");
			}

			ThreadContext.setApplication(application);
			try
			{
				application.initApplication();

				// Give the application the option to log that it is started
				application.logStarted();
			}
			finally
			{
				ThreadContext.detach();
			}
		}
		catch (Exception e)
		{
			// #destroy() might not be called by the web container when #init() fails,
			// so destroy now
			log.warn("initialization failed, destroying now");

			try
			{
				destroy();
			}
			catch (Exception destroyException)
			{
				log.warn("Unable to destroy after initialization failure", destroyException);
			}

			throw new ServletException(e);
		}
		finally
		{
			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		}
	}
	public void destroy()
	{
		if (application != null)
		{
			try
			{
				ThreadContext.setApplication(application);
				application.internalDestroy();
			}
			finally
			{
				ThreadContext.detach();
				application = null;
			}
		}

		if (applicationFactory != null)
		{
			try
			{
				applicationFactory.destroy(this);
			}
			finally
			{
				applicationFactory = null;
			}
		}
	}
	public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception)
	{
		super.onExceptionRequestHandlerResolved(cycle, handler, exception);
		registerLastHandler(cycle,handler);
	}
