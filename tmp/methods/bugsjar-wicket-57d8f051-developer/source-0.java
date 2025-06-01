	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		CharSequence js = getCallbackScript();
		js = JavaScriptUtils.escapeQuotes(js);

		String timeoutHandle = getTimeoutHandle();
		// this might look strange, but it is necessary for IE not to leak :(
		return timeoutHandle+" = setTimeout('" + js + "', " +
			updateInterval.getMilliseconds() + ')';
	}
	private void clearTimeout(IHeaderResponse headerResponse)
	{
		if (hasTimeout)
		{
			hasTimeout = false;

			String timeoutHandle = getTimeoutHandle();
			headerResponse.render(OnLoadHeaderItem.forScript("clearTimeout(" + timeoutHandle
				+ "); delete " + timeoutHandle + ";"));
		}
	}
	private String getTimeoutHandle() {
		return "Wicket.TimerHandles['"+getComponent().getMarkupId() + "']";
	}
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forScript(
			"if (typeof(Wicket.TimerHandles) === 'undefined') {Wicket.TimerHandles = {}}",
			WICKET_TIMERS_ID));

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
