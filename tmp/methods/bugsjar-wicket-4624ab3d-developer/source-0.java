	public void respond(final IRequestCycle requestCycle)
	{
		final IRequestablePage page = getPage();
		if (getComponent().getPage() == page)
		{
			boolean isAjax = ((WebRequest)requestCycle.getRequest()).isAjax();
			if (isAjax == false && listenerInterface.isRenderPageAfterInvocation())
			{
				// schedule page render after current request handler is done. this can be
				// overridden during invocation of listener
				// method (i.e. by calling RequestCycle#setResponsePage)
				final IPageProvider pageProvider = new PageProvider(page);
				final RedirectPolicy policy = page.isPageStateless()
					? RedirectPolicy.NEVER_REDIRECT : RedirectPolicy.AUTO_REDIRECT;

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
