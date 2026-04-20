	public Url mapHandler(IRequestHandler requestHandler)
	{
		Url url = super.mapHandler(requestHandler);

		if (url == null && requestHandler instanceof ListenerInterfaceRequestHandler)
		{
			ListenerInterfaceRequestHandler handler = (ListenerInterfaceRequestHandler)requestHandler;
			IRequestablePage page = handler.getPage();
			if (checkPageInstance(page))
			{
				String componentPath = handler.getComponentPath();
				RequestListenerInterface listenerInterface = handler.getListenerInterface();

				Integer renderCount = null;
				if (listenerInterface.isIncludeRenderCount())
				{
					renderCount = page.getRenderCount();
				}

				PageInfo pageInfo = new PageInfo(page.getPageId());
				ComponentInfo componentInfo = new ComponentInfo(renderCount,
					requestListenerInterfaceToString(listenerInterface), componentPath,
					handler.getBehaviorIndex());
				PageComponentInfo pageComponentInfo = new PageComponentInfo(pageInfo, componentInfo);
				UrlInfo urlInfo = new UrlInfo(pageComponentInfo, page.getClass(),
					handler.getPageParameters());
				url = buildUrl(urlInfo);
			}
		}

		return url;
	}
