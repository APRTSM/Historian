	protected IRequestHandler processHybrid(PageInfo pageInfo,
		Class<? extends IRequestablePage> pageClass, PageParameters pageParameters,
		Integer renderCount)
	{
		PageProvider provider = new PageProvider(pageInfo.getPageId(), pageClass, pageParameters,
			renderCount);
		provider.setPageSource(getContext());
		if (provider.isNewPageInstance() && !getRecreateMountedPagesAfterExpiry())
		{
			throw new PageExpiredException(String.format("Bookmarkable page id '%d' has expired.",
				pageInfo.getPageId()));
		}
		else
		{
			/**
			 * https://issues.apache.org/jira/browse/WICKET-5734
			 * */
			PageParameters constructionPageParameters = provider.hasPageInstance() ?
				provider.getPageInstance().getPageParameters() : new PageParameters();

			if (PageParameters.equals(constructionPageParameters, pageParameters) == false)
			{
				// create a fresh page instance because the request page parameters are different than the ones
				// when the resolved page by id has been created
				return new RenderPageRequestHandler(new PageProvider(pageClass, pageParameters));
			}
			return new RenderPageRequestHandler(provider);
		}
	}
