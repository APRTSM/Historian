	public int getCompatibilityScore(final Request request)
	{
		int score = 0;
		Url url = request.getUrl();
		if (matches(url))
		{
			score = Integer.MAX_VALUE;
		}
		return score;
	}
	private boolean matches(final Url url)
	{
		return urlStartsWith(url, getContext().getNamespace(), getContext().getPageIdentifier());
	}
	public IRequestHandler mapRequest(Request request)
	{
		Url url = request.getUrl();
		if (matches(url))
		{
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
