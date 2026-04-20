	public final CharSequence urlFor(final Behavior behaviour,
		final RequestListenerInterface listener)
	{
		int id = getBehaviorId(behaviour);
		Page page = getPage();
		IRequestHandler handler;
		if (page.isPageStateless())
		{
			PageAndComponentProvider provider = new PageAndComponentProvider(page.getPageClass(),
				page.getPageParameters(), getPageRelativePath());
			handler = new BookmarkableListenerInterfaceRequestHandler(provider, listener, id);
		}
		else
		{
			PageAndComponentProvider provider = new PageAndComponentProvider(page, this);
			handler = new ListenerInterfaceRequestHandler(provider, listener, id);
		}
		return getRequestCycle().urlFor(handler);
	}
	public final CharSequence urlFor(final RequestListenerInterface listener)
	{
		Page page = getPage();
		IRequestHandler handler;
		if (page.isPageStateless())
		{
			PageAndComponentProvider provider = new PageAndComponentProvider(page.getPageClass(),
				page.getPageParameters(), getPageRelativePath());
			handler = new BookmarkableListenerInterfaceRequestHandler(provider, listener);
		}
		else
		{
			PageAndComponentProvider provider = new PageAndComponentProvider(page, this);
			handler = new ListenerInterfaceRequestHandler(provider, listener);
		}
		return getRequestCycle().urlFor(handler);
	}
