	public final boolean isPageInstanceFresh()
	{
		if (!hasPageInstance())
		{
			throw new IllegalStateException("Page instance not yet resolved");
		}
		return pageInstanceIsFresh;
	}
	private IRequestablePage getStoredPage(final int pageId)
	{
		IRequestablePage storedPageInstance = getPageSource().getPageInstance(pageId);
		if (storedPageInstance != null)
		{
			if (pageClass == null || pageClass.equals(storedPageInstance.getClass()))
			{
				pageInstance = storedPageInstance;
				pageInstanceIsFresh = false;
				if (renderCount != null && pageInstance.getRenderCount() != renderCount)
				{
					throw new StalePageException(pageInstance);
				}
			}
			else
			{
				// the found page class doesn't match the requested one
				storedPageInstance = null;
			}
		}
		return storedPageInstance;
	}
