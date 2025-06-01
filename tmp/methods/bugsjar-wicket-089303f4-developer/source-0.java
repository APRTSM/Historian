	public final void postProcessRequestCycle(WebRequestCycle cycle)
	{
		previousRenderedPage = lastRenderedPage;

		if (cycle.getResponse() instanceof WebResponse)
		{
			// handle redirects which are usually managed by the browser
			// transparently
			final MockHttpServletResponse httpResponse = (MockHttpServletResponse)cycle.getWebResponse()
				.getHttpServletResponse();

			if (httpResponse.isRedirect())
			{
				lastRenderedPage = generateLastRenderedPage(cycle);

				MockHttpServletRequest newHttpRequest = new MockHttpServletRequest(application,
					servletSession, application.getServletContext());
				newHttpRequest.setRequestToRedirectString(httpResponse.getRedirectLocation());
				wicketRequest = application.newWebRequest(newHttpRequest);

				cycle = createRequestCycle();
				cycle.request();
			}
		}
		lastRenderedPage = generateLastRenderedPage(cycle);

		Session.set(getWicketSession());

		if (getLastRenderedPage() instanceof ExceptionErrorPage)
		{
			throw (RuntimeException)((ExceptionErrorPage)getLastRenderedPage()).getThrowable();
		}
	}
