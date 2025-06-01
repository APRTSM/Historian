	protected BufferedWebResponse renderPage(Url targetUrl, RequestCycle requestCycle)
	{
		IRequestHandler scheduled = requestCycle.getRequestHandlerScheduledAfterCurrent();

		// keep the original response
		final WebResponse originalResponse = (WebResponse) requestCycle.getResponse();

		// buffered web response for page
		BufferedWebResponse response = new BufferedWebResponse(originalResponse);

		// keep the original base URL
		Url originalBaseUrl = requestCycle.getUrlRenderer().setBaseUrl(targetUrl);

		try
		{
			requestCycle.setResponse(response);
			getPage().renderPage();

			if (scheduled == null && requestCycle.getRequestHandlerScheduledAfterCurrent() != null)
			{
				// This is a special case.
				// During page render another request handler got scheduled and will want to overwrite
				// the response, so we need to let it.
				// Just preserve the meta data headers
				originalResponse.reset(); // clear the initial actions because they are already copied into the new response's actions
				response.writeMetaData(originalResponse);
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
