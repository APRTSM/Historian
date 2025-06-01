	protected final void respond(final AjaxRequestTarget target)
	{
		onTimer(target);

		if (!stopped)
		{
			// this might look strange, but it is necessary for IE not to leak
			String js = "setTimeout(\"" + getCallbackScript(false, true) + "\", "
					+ updateInterval.getMilliseconds() + ");";

			target.appendJavascript(js);
		}
	}
	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		return "setTimeout(function() { " + getCallbackScript(false, true) + " }, "
				+ updateInterval.getMilliseconds() + ");";
	}
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		if (this.attachedBodyOnLoadModifier == false)
		{
			this.attachedBodyOnLoadModifier = true;
			((WebPage)getComponent().getPage()).getBodyContainer().addOnLoadModifier(
					getJsTimeoutCall(updateInterval), getComponent());
		}
	}
