	public final void respond(final IRequestCycle requestCycle)
	{
		// do not increment page id during ajax processing
		boolean frozen = page.setFreezePageId(true);

		try
		{
			final RequestCycle rc = (RequestCycle)requestCycle;
			final WebResponse response = (WebResponse)requestCycle.getResponse();

			if (markupIdToComponent.values().contains(page))
			{
				// the page itself has been added to the request target, we simply issue a redirect
				// back to the page
				IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(page));
				final String url = rc.urlFor(handler).toString();
				response.sendRedirect(url);
				return;
			}

			for (ITargetRespondListener listener : respondListeners)
			{
				listener.onTargetRespond(this);
			}

			final Application app = Application.get();

			page.send(app, Broadcast.BREADTH, this);

			// Determine encoding
			final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

			// Set content type based on markup type for page
			response.setContentType("text/xml; charset=" + encoding);

			// Make sure it is not cached by a client
			response.disableCaching();

			try
			{
				final StringResponse bodyResponse = new StringResponse();
				contructResponseBody(bodyResponse, encoding);
				CharSequence filteredResponse = invokeResponseFilters(bodyResponse);
				response.write(filteredResponse);
			}
			finally
			{
				// restore the original response
				RequestCycle.get().setResponse(response);
			}
		}
		finally
		{
			page.setFreezePageId(frozen);
		}
	}
	private AppendingStringBuffer invokeResponseFilters(final StringResponse contentResponse)
	{
		AppendingStringBuffer responseBuffer = new AppendingStringBuffer(
			contentResponse.getBuffer());

		List<IResponseFilter> responseFilters = Application.get()
			.getRequestCycleSettings()
			.getResponseFilters();

		if (responseFilters != null)
		{
			for (IResponseFilter filter : responseFilters)
			{
				responseBuffer = filter.filter(responseBuffer);
			}
		}
		return responseBuffer;
	}
		protected void invoke(WebResponse response)
		{

			AppendingStringBuffer responseBuffer = new AppendingStringBuffer(builder);

			List<IResponseFilter> responseFilters = Application.get()
				.getRequestCycleSettings()
				.getResponseFilters();

			if (responseFilters != null)
			{
				for (IResponseFilter filter : responseFilters)
				{
					responseBuffer = filter.filter(responseBuffer);
				}
			}
			response.write(responseBuffer);
		}
