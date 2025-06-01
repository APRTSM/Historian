	public void setResponsePage(Class<? extends IRequestablePage> pageClass,
		PageParameters parameters)
	{
		IPageProvider provider = new PageProvider(pageClass, parameters);
		scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(provider,
			RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
	}
	public void setResponsePage(Class<? extends IRequestablePage> pageClass)
	{
		IPageProvider provider = new PageProvider(pageClass, null);
		scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(provider,
			RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
	}
