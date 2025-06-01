	protected BufferedWebResponse renderPage(Url targetUrl, RequestCycle requestCycle)
	{
		IRequestHandler scheduled = requestCycle.getRequestHandlerScheduledAfterCurrent();

		// keep the original response
		final Response originalResponse = requestCycle.getResponse();

		// buffered web response for page
		BufferedWebResponse response = new BufferedWebResponse((WebResponse)originalResponse);

		// keep the original base URL
		Url originalBaseUrl = requestCycle.getUrlRenderer().setBaseUrl(targetUrl);

		try
		{
			requestCycle.setResponse(response);
			getPage().renderPage();

			if (scheduled == null && requestCycle.getRequestHandlerScheduledAfterCurrent() != null)
			{
				// This is a special case. During page render another request handler got scheduled.
				// The handler
				// will want to overwrite the response, so we need to let it
				return null;
			}
			else
			{
				return response;
			}
		}
		finally
		{
			// restore original response and base URL
			requestCycle.setResponse(originalResponse);
			requestCycle.getUrlRenderer().setBaseUrl(originalBaseUrl);
		}
	}
