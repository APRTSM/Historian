	public void respond(final IRequestCycle requestCycle)
	{
		final boolean isNewPageInstance = pageComponentProvider.isNewPageInstance();
		final boolean isAjax = ((WebRequest)requestCycle.getRequest()).isAjax();
		final IRequestablePage page = getPage();
		final boolean isStateless = page.isPageStateless();
		final IPageProvider pageProvider = new PageProvider(page);

		if (getComponent().getPage() == page)
		{
			RedirectPolicy policy = isStateless ? RedirectPolicy.NEVER_REDIRECT
				: RedirectPolicy.AUTO_REDIRECT;

			if (isNewPageInstance)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(
						"A ListenerInterface '{}' assigned to '{}' is executed on an expired page. "
							+ "Scheduling re-create of the page and ignoring the listener interface...",
						listenerInterface, getComponentPath());
				}

				if (isAjax)
				{
					policy = RedirectPolicy.ALWAYS_REDIRECT;
				}

				requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(
					pageProvider, policy));
				return;
			}

			if (isAjax == false && listenerInterface.isRenderPageAfterInvocation())
			{
				// schedule page render after current request handler is done. this can be
				// overridden during invocation of listener
				// method (i.e. by calling RequestCycle#setResponsePage)
				requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(
					pageProvider, policy));
			}

			invokeListener();

		}
		else
		{
			throw new WicketRuntimeException("Component " + getComponent() +
				" has been removed from page.");
		}
	}
	private IRequestablePage getStoredPage(final int pageId)
	{
		IRequestablePage storedPageInstance = getPageSource().getPageInstance(pageId);
		if (storedPageInstance != null &&
			(pageClass == null || pageClass.equals(storedPageInstance.getClass())))
		{
			pageInstance = storedPageInstance;

			if (pageInstance != null)
			{
				if (renderCount != null && pageInstance.getRenderCount() != renderCount)
				{
					throw new StalePageException(pageInstance);
				}
			}
		}
		return storedPageInstance;
	}
	public boolean isNewPageInstance()
	{
		if (isNewInstance == null)
		{
			isNewInstance = pageInstance == null;
			if (isNewInstance && pageId != null)
			{
				IRequestablePage storedPageInstance = getStoredPage(pageId);
				if (storedPageInstance != null)
				{
					pageInstance = storedPageInstance;
					isNewInstance = false;
				}
			}
		}
		return isNewInstance;
	}
