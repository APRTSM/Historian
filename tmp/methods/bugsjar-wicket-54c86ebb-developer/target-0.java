	private IRequestablePage getStoredPage(final int pageId)
	{
		IRequestablePage storedPageInstance = getPageSource().getPageInstance(pageId);
		if (storedPageInstance != null)
		{
			if (
				(pageClass == null || pageClass.equals(storedPageInstance.getClass())) &&
				(isPageParametersEmpty(pageParameters) || arePageParametersSame(storedPageInstance))
			)
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
	private boolean arePageParametersSame(IRequestablePage storedPageInstance) {
		PageParameters currentCopy = new PageParameters(pageParameters);
		PageParameters storedCopy = new PageParameters(storedPageInstance.getPageParameters());
		return currentCopy.equals(storedCopy);
	}
	private boolean isPageParametersEmpty(PageParameters parameters)
	{
		return parameters == null || parameters.isEmpty();
	}
