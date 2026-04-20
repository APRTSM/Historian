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
