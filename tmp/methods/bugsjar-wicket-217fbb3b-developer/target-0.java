	protected void writeHeaderContribution(final Response response, final Component component)
	{
		headerRendering = true;

		// create the htmlheadercontainer if needed
		if (header == null)
		{
			header = new AjaxHtmlHeaderContainer(this);
			final Page parentPage = component.getPage();
			parentPage.addOrReplace(header);
		}

		RequestCycle requestCycle = component.getRequestCycle();

		// save old response, set new
		Response oldResponse = requestCycle.setResponse(encodingHeaderResponse);

		try {
			encodingHeaderResponse.reset();

			IHeaderRenderStrategy strategy = AbstractHeaderRenderStrategy.get();

			strategy.renderHeader(header, null, component);
		} finally {
			// revert to old response
			requestCycle.setResponse(oldResponse);
		}

		writeHeaderContribution(response);

		headerRendering = false;
	}
