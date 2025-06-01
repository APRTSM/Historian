	private void resolvePageInstance(Integer pageId, Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters, Integer renderCount)
	{
		IRequestablePage page = null;

		boolean freshCreated = false;

		if (pageId != null)
		{
			page = getStoredPage(pageId);
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
