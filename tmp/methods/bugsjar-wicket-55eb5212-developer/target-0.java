	public static CharSequence renderComponent(final Component component)
	{
		RequestCycle requestCycle = RequestCycle.get();

		final Response originalResponse = requestCycle.getResponse();
		BufferedWebResponse tempResponse = new BufferedWebResponse(null);

		try
		{
			requestCycle.setResponse(tempResponse);

			RenderPage page = new RenderPage();
			page.add(component);
			page.internalInitialize();

			component.render();
		}
		finally
		{
			requestCycle.setResponse(originalResponse);
		}

		return tempResponse.getText();
	}
