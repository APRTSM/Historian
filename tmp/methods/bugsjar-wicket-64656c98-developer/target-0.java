	public void beforeRender(Component component)
	{
		super.beforeRender(component);

		final RequestCycle requestCycle = RequestCycle.get();

		// Temporarily replace the web response with a String response
		originalResponse = requestCycle.getResponse();

		WebResponse origResponse = (WebResponse)((originalResponse instanceof WebResponse)
			? originalResponse : null);
		BufferedWebResponse tempResponse = newResponse(origResponse);

		// temporarily set StringResponse to collect the transformed output
		requestCycle.setResponse(tempResponse);
	}
	public void afterRender(final Component component)
	{
		final RequestCycle requestCycle = RequestCycle.get();

		try
		{
			BufferedWebResponse tempResponse = (BufferedWebResponse)requestCycle.getResponse();

			// Transform the data
			CharSequence output = transform(component, tempResponse.getText());
			originalResponse.write(output);
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException("Error while transforming the output of component: " +
				component, ex);
		}
		finally
		{
			// Restore the original response object
			requestCycle.setResponse(originalResponse);
		}
	}
	public void detach(Component component)
	{
		originalResponse = null;
		super.detach(component);
	}
