		protected void invoke(WebResponse response)
		{
			writeStream(response, stream);
		}
	public void writeTo(final WebResponse response)
	{
		Args.notNull(response, "response");

		for (Action action : actions)
		{
			action.invoke(response);
		}
	}
		protected abstract void invoke(WebResponse response);
	}

	/**
	 * Actions not related directly to the content of the response, eg setting cookies, headers.
	 * 
	 * @author igor
	 */
	private static abstract class MetaDataAction extends Action
	{
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
					filter.filter(responseBuffer);
				}
			}
			response.write(builder);
		}
