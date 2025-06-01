	protected boolean shouldRenderPageAndWriteResponse(RequestCycle cycle, Url currentUrl,
		Url targetUrl)
	{
		// WICKET-5484 never render and write for Ajax requests
		if (isAjax(cycle))
		{
			return false;
		}

		return neverRedirect(getRedirectPolicy())
			|| ((isOnePassRender() && notForcedRedirect(getRedirectPolicy())) || (targetUrl
				.equals(currentUrl) && notNewAndNotStatelessPage(isNewPageInstance(),
				isPageStateless()))) || (targetUrl.equals(currentUrl) && isRedirectToRender())
			|| (shouldPreserveClientUrl(cycle) && notForcedRedirect(getRedirectPolicy()));
	}
