	public final CharSequence urlFor(final Behavior behaviour,
		final RequestListenerInterface listener, final PageParameters parameters)
	{
		int id = getBehaviorId(behaviour);
		Page page = getPage();
		PageAndComponentProvider provider = new PageAndComponentProvider(page, this, parameters);
		IRequestHandler handler;
		if (page.isBookmarkable())
		{
			handler = new BookmarkableListenerInterfaceRequestHandler(provider, listener, id);
		}
		else
		{
			handler = new ListenerInterfaceRequestHandler(provider, listener, id);
		}
		return getRequestCycle().urlFor(handler);
	}
	public final CharSequence urlFor(final RequestListenerInterface listener,
		final PageParameters parameters)
	{
		Page page = getPage();
		PageAndComponentProvider provider = new PageAndComponentProvider(page, this, parameters);
		IRequestHandler handler;
		if (page.isBookmarkable())
		{
			handler = new BookmarkableListenerInterfaceRequestHandler(provider, listener);
		}
		else
		{
			handler = new ListenerInterfaceRequestHandler(provider, listener);
		}
		return getRequestCycle().urlFor(handler);
	}
