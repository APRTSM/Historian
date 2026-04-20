	private IRequestablePage getStoredPage(final int pageId)
	{
		IRequestablePage storedPageInstance = getPageSource().getPageInstance(pageId);
		if (storedPageInstance != null &&
			(pageClass == null || pageClass.equals(storedPageInstance.getClass())))
		{
			pageInstance = storedPageInstance;
			pageInstanceIsFresh = false;
			if (pageInstance != null)
			{
				if (renderCount != null && pageInstance.getRenderCount() != renderCount)
				{
					throw new StalePageException(pageInstance);
				}
			}
		}
		return storedPageInstance;
	}
