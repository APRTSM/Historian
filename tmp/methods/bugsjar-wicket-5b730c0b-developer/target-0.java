	protected boolean compatibleProtocols(String p1, String p2)
	{
		if (p1 != null && p2 != null)
		{
			return Objects.equal(p1, p2);
		}

		return true;
	}
	protected boolean shouldRenderPageAndWriteResponse(RequestCycle cycle, Url currentUrl,
		Url targetUrl)
	{
		// WICKET-5484 never render and write for Ajax requests
		if (isAjax(cycle))
		{
			return false;
		}

		return (compatibleProtocols(currentUrl.getProtocol(), targetUrl.getProtocol())) &&
				(neverRedirect(getRedirectPolicy())
			|| ((isOnePassRender() && notForcedRedirect(getRedirectPolicy())) || (targetUrl
				.equals(currentUrl) && notNewAndNotStatelessPage(isNewPageInstance(),
				isPageStateless()))) || (targetUrl.equals(currentUrl) && isRedirectToRender())
			|| (shouldPreserveClientUrl(cycle) && notForcedRedirect(getRedirectPolicy())));
	}
