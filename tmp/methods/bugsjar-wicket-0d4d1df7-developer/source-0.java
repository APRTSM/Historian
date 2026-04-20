	protected void redirectTo(Url url, RequestCycle requestCycle)
	{
		WebResponse response = (WebResponse)requestCycle.getResponse();
		String relativeUrl = requestCycle.getUrlRenderer().renderUrl(url);
		response.sendRedirect(relativeUrl);
	}
