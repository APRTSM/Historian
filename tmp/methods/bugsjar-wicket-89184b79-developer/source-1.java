	private void resolvePageInstance(Integer pageId, Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters, Integer renderCount)
	{
		IRequestablePage page = null;

		boolean freshCreated = false;

		if (pageId != null)
		{
			page = getStoredPage(pageId);

			if (page == null)
			{
				// WICKET-4594 - ignore the parsed parameters for stateful pages
				pageParameters = null;
			}
		}

		if (page == null)
		{
			if (pageClass != null)
			{
				page = getPageSource().newPageInstance(pageClass, pageParameters);
				freshCreated = true;
			}
		}

		if (page != null && !freshCreated)
		{
			if (renderCount != null && page.getRenderCount() != renderCount)
			{
				throw new StalePageException(page);
			}
		}

		pageInstanceIsFresh = freshCreated;
		pageInstance = page;
	}
	protected IRequestHandler processListener(PageComponentInfo pageComponentInfo,
		Class<? extends IRequestablePage> pageClass, PageParameters pageParameters)
	{
		PageInfo pageInfo = pageComponentInfo.getPageInfo();
		ComponentInfo componentInfo = pageComponentInfo.getComponentInfo();
		Integer renderCount = null;
		RequestListenerInterface listenerInterface = null;

		if (componentInfo != null)
		{
			renderCount = componentInfo.getRenderCount();
			listenerInterface = requestListenerInterfaceFromString(componentInfo.getListenerInterface());
		}

		if (listenerInterface != null)
		{
//			if (pageInfo.getPageId() != null)
//			{
//				// WICKET-4594 - ignore the parsed parameters for stateful pages
//				pageParameters = null;
//			}

			PageAndComponentProvider provider = new PageAndComponentProvider(pageInfo.getPageId(),
				pageClass, pageParameters, renderCount, componentInfo.getComponentPath());

			provider.setPageSource(getContext());

			return new ListenerInterfaceRequestHandler(provider, listenerInterface,
				componentInfo.getBehaviorId());
		}
		else
		{
			if (logger.isWarnEnabled())
			{
				if (componentInfo != null)
				{
					logger.warn("Unknown listener interface '{}'",
						componentInfo.getListenerInterface());
				}
				else
				{
					logger.warn("Cannot extract the listener interface for PageComponentInfo: '{}'" +
						pageComponentInfo);
				}
			}
			return null;
		}
	}
