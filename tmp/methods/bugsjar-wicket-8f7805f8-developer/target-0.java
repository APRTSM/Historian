	private IRequestablePage getStoredPage(final int pageId)
	{
		IRequestablePage storedPageInstance = getPageSource().getPageInstance(pageId);
		if (storedPageInstance != null &&
			(pageClass == null || pageClass.equals(storedPageInstance.getClass())))
		{
			pageInstance = storedPageInstance;
		}
		return storedPageInstance;
	}
