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
			// If the page was stale, just rerender it
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
				// we don't wan't to cache an exceptional reply in the browser
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
		if (!page.isPageStateless())
		{
			getContext().bind();
		}
		getRequestAdapter().touch(page);
	}
