	protected final String getJsTimeoutCall(final Duration updateInterval)
	{
		// this might look strange, but it is necessary for IE not to leak :(
		return "setTimeout(\"" + getCallbackScript(false, true) + "\", "
				+ updateInterval.getMilliseconds() + ");";
	}
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		if (this.attachedBodyOnLoadModifier == false)
		{
			this.attachedBodyOnLoadModifier = true;
			if (RequestCycle.get().getRequestTarget() instanceof AjaxRequestTarget) {
				response.renderJavascript(getJsTimeoutCall(updateInterval), getComponent().getMarkupId());
			}
			else
			{
				((WebPage)getComponent().getPage()).getBodyContainer().addOnLoadModifier(
						getJsTimeoutCall(updateInterval), getComponent());
			}
		}
	}
	protected final void respond(final AjaxRequestTarget target)
	{
		onTimer(target);

		if (!stopped)
		{
			target.appendJavascript(getJsTimeoutCall(updateInterval));
		}
	}
