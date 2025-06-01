	public RestartResponseAtInterceptPageException(Class<? extends Page> interceptPageClass,
		PageParameters parameters)
	{
		super(new RenderPageRequestHandler(new PageProvider(interceptPageClass, parameters),
			RedirectPolicy.AUTO_REDIRECT));
		InterceptData.set();
	}
	public RestartResponseAtInterceptPageException(Page interceptPage)
	{
		super(new RenderPageRequestHandler(new PageProvider(interceptPage),
			RedirectPolicy.AUTO_REDIRECT));
		InterceptData.set();
	}
