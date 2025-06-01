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
				getHeaderResponse().render(StringHeaderItem.forString(bodyOutput));
			}
		}
		finally
		{
			getRequestCycle().setResponse(oldResponse);
		}
	}
	protected final boolean renderNext(MarkupStream markupStream)
	{
		StringResponse markupHeaderResponse = new StringResponse();
		Response oldResponse = getResponse();
		RequestCycle.get().setResponse(markupHeaderResponse);
		try
		{
			boolean ret = super.renderNext(markupStream);
			getHeaderResponse().render(new PageHeaderItem(markupHeaderResponse.getBuffer()));
			return ret;
		}
		finally
		{
			RequestCycle.get().setResponse(oldResponse);
		}
	}
