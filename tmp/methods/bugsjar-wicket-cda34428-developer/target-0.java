	public void renderHeaderTagBody(HeaderStreamState headerStreamState)
	{
		if (headerStreamState == null)
			return;

		final Response oldResponse = getRequestCycle().getResponse();
		try
		{
			// Create a separate (string) response for the header container itself
			final StringResponse bodyResponse = new StringResponse();
			getRequestCycle().setResponse(bodyResponse);

			// render the header section directly associated with the markup
			super.onComponentTagBody(headerStreamState.getMarkupStream(),
				headerStreamState.getOpenTag());
			CharSequence bodyOutput = getCleanResponse(bodyResponse);
			if (bodyOutput.length() > 0)
			{
				getHeaderResponse().render(new PageHeaderItem(bodyOutput));
			}
		}
		finally
		{
			getRequestCycle().setResponse(oldResponse);
		}
	}
