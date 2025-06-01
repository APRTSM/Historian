	protected boolean shouldRenderPageAndWriteResponse(RequestCycle cycle, Url currentUrl,
		Url targetUrl)
	{
		return neverRedirect(getRedirectPolicy())
			|| (!isAjax(cycle) && ((isOnePassRender() && notForcedRedirect(getRedirectPolicy())) || (targetUrl
				.equals(currentUrl) && notNewAndNotStatelessPage(isNewPageInstance(),
				isPageStateless())))) || (targetUrl.equals(currentUrl) && isRedirectToRender())
			|| shouldPreserveClientUrl(cycle);
	}
