	protected IRequestHandler processHybrid(PageInfo pageInfo,
		Class<? extends IRequestablePage> pageClass, PageParameters pageParameters,
		Integer renderCount)
	{
		PageProvider provider = new PageProvider(pageInfo.getPageId(), pageClass, pageParameters,
			renderCount);
		provider.setPageSource(getContext());
		if (provider.isNewPageInstance() &&
			!WebApplication.get().getPageSettings().getRecreateMountedPagesAfterExpiry())
		{
			throw new PageExpiredException(String.format("Bookmarkable page id '%d' has expired.",
				pageInfo.getPageId()));
		}
		else
		{
			return new RenderPageRequestHandler(provider);
		}
	}
