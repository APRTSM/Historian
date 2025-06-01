	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		// only add the event handler when the component is enabled.
		Component myComponent = getComponent();
		if (myComponent.isEnabledInHierarchy())
		{
			tag.put(event, escapeAttribute(getEventHandler()));
		}
	}
	private CharSequence escapeAttribute(final CharSequence attr)
	{
		if(null == attr)
		{
			return null;
		}
		CharSequence escaped = Strings.escapeMarkup(attr.toString());
		// No need to escape the apostrophe; it just clutters the markup
		return Strings.replaceAll(escaped, "&#039;", "'");
	}
