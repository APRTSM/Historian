	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		// only add the event handler when the component is enabled.
		Component myComponent = getComponent();
		if (myComponent.isEnabledInHierarchy())
		{
			tag.put(event, getEventHandler());
		}
	}
	protected CharSequence getPreconditionScript()
	{
		return "return Wicket.$$(this)&amp;&amp;Wicket.$$('" + getForm().getMarkupId() + "')";
	}
