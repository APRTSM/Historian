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
				PageParameters parameters;
				if (pageId != null)
				{
					// WICKET-4594 - re-creating an expired page. Ignore the parsed parameters for the callback url
					parameters = new PageParameters();
				}
				else
				{
					parameters = pageParameters;
				}
				page = getPageSource().newPageInstance(pageClass, parameters);
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
