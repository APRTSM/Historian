	public int getCompatibilityScore(final Request request)
	{
		int score = 0;
		if (matches(request))
		{
			score = Integer.MAX_VALUE;
		}
		return score;
	}
	private boolean matches(final Request request)
	{
		boolean matches = false;
		Url url = request.getUrl();
		String namespace = getContext().getNamespace();
		String pageIdentifier = getContext().getPageIdentifier();
		if (urlStartsWith(url, namespace, pageIdentifier))
		{
			matches = true;
		}
		else if (urlStartsWith(request.getClientUrl(), namespace) && urlStartsWith(url, pageIdentifier))
		{
			matches = true;
		}

		return matches;
	}
	public IRequestHandler mapRequest(Request request)
	{
		if (matches(request))
		{
			Url url = request.getUrl();
			PageComponentInfo info = getPageComponentInfo(url);
			if (info != null && info.getPageInfo().getPageId() != null)
			{
				Integer renderCount = info.getComponentInfo() != null ? info.getComponentInfo()
					.getRenderCount() : null;

				if (info.getComponentInfo() == null)
				{
					PageProvider provider = new PageProvider(info.getPageInfo().getPageId(),
						renderCount);
					provider.setPageSource(getContext());
					// render page
					return new RenderPageRequestHandler(provider);
				}
				else
				{
					ComponentInfo componentInfo = info.getComponentInfo();

					PageAndComponentProvider provider = new PageAndComponentProvider(
						info.getPageInfo().getPageId(), renderCount,
						componentInfo.getComponentPath());

					provider.setPageSource(getContext());

					// listener interface
					RequestListenerInterface listenerInterface = requestListenerInterfaceFromString(componentInfo.getListenerInterface());

					return new ListenerInterfaceRequestHandler(provider, listenerInterface,
						componentInfo.getBehaviorId());
				}
			}
		}
		return null;
	}
