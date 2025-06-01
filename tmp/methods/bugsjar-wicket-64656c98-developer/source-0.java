	public AbstractTransformerBehavior()
	{
	}
	public void detach(Component component)
	{
		webResponse = null;
		super.detach(component);
	}
	public void beforeRender(Component component)
	{
		super.beforeRender(component);

		final RequestCycle requestCycle = RequestCycle.get();

		// Temporarily replace the web response with a String response
		webResponse = (WebResponse)requestCycle.getResponse();

		// Create a new response object
		final BufferedWebResponse response = newResponse(webResponse);
		if (response == null)
		{
			throw new IllegalStateException("newResponse() must not return null");
		}

		// and make it the current one
		requestCycle.setResponse(response);
	}
	public void afterRender(final Component component)
	{
		final RequestCycle requestCycle = RequestCycle.get();

		try
		{
			BufferedWebResponse response = (BufferedWebResponse)requestCycle.getResponse();

			// Transform the data
			CharSequence output = transform(component, response.getText());
			response.setText(output);
			response.writeTo(webResponse);
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException("Error while transforming the output: " + this, ex);
		}
		finally
		{
			// Restore the original response object
			requestCycle.setResponse(webResponse);
		}
	}
