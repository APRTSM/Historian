	private void clearTimeout(IHeaderResponse headerResponse)
	{
		if (hasTimeout)
		{
			hasTimeout = false;

			headerResponse.render(OnLoadHeaderItem.forScript("Wicket.Timer.clear('" + getComponent().getMarkupId() + "');"));
		}
	}
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		if (component.getRequestCycle().find(AjaxRequestTarget.class) == null)
		{
			// complete page is rendered, so timeout has to be rendered again
			hasTimeout = false;
		}

		if (isStopped() == false)
		{
			addTimeout(response);
		}
	}
	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		CharSequence js = getCallbackScript();

		return String.format("Wicket.Timer.set('%s', function(){%s}, %d);",
				getComponent().getMarkupId(), js, updateInterval.getMilliseconds());
	}
