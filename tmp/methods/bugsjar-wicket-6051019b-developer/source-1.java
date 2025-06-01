	public final CharSequence urlFor(final RequestListenerInterface listener)
	{
		PageAndComponentProvider provider = new PageAndComponentProvider(getPage(), this);
		IRequestHandler handler;
		if (getPage().isPageStateless())
		{
			handler = new BookmarkableListenerInterfaceRequestHandler(provider, listener);
		}
		else
		{
			handler = new ListenerInterfaceRequestHandler(provider, listener);
		}
		return getRequestCycle().urlFor(handler);
	}
	public final CharSequence urlFor(final Behavior behaviour,
		final RequestListenerInterface listener)
	{
		PageAndComponentProvider provider = new PageAndComponentProvider(getPage(), this);
		int id = getBehaviorId(behaviour);
		IRequestHandler handler;
		if (getPage().isPageStateless())
		{
			handler = new BookmarkableListenerInterfaceRequestHandler(provider, listener, id);
		}
		else
		{
			handler = new ListenerInterfaceRequestHandler(provider, listener, id);
		}
		return getRequestCycle().urlFor(handler);
	}
	public void touchPage(IManageablePage page)
	{
		pages.put(page.getPageId(), page);
	}
