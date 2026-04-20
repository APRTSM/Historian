	public void respond(final IRequestCycle requestCycle)
	{
		final IRequestablePage page = getPage();
		final boolean freshPage = pageComponentProvider.isPageInstanceFresh();
		final boolean isAjax = ((WebRequest)requestCycle.getRequest()).isAjax();

		IRequestableComponent component;
		try
		{
			component = getComponent();
		}
		catch (ComponentNotFoundException e)
		{
			// either the page is stateless and the component we are looking for is not added in the
			// constructor
			// or the page is stateful+stale and a new instances was created by pageprovider
			// we denote this by setting component to null
			component = null;
		}

		if ((component == null && !freshPage) || (component != null && component.getPage() != page))
		{
			throw new WicketRuntimeException("Component '" + getComponentPath()
				+ "' has been removed from page.");
		}

		if (page instanceof Page)
		{
			// initialize the page to be able to check whether it is stateless
			((Page)page).internalInitialize();
		}
		final boolean isStateless = page.isPageStateless();

		RedirectPolicy policy = isStateless
			? RedirectPolicy.NEVER_REDIRECT
			: RedirectPolicy.AUTO_REDIRECT;
		final IPageProvider pageProvider = new PageProvider(page);

		if (freshPage && (isStateless == false || component == null))
		{
			// A listener interface is invoked on an expired page.

			// If the page is stateful then we cannot assume that the listener interface is
			// invoked on its initial state (right after page initialization) and that its
			// component and/or behavior will be available. That's why the listener interface
			// should be ignored and the best we can do is to re-paint the newly constructed
			// page.

			if (LOG.isDebugEnabled())
			{
				LOG.debug(
					"A ListenerInterface '{}' assigned to '{}' is executed on an expired stateful page. "
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
