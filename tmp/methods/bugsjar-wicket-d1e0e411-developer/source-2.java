	protected void onAfterRenderChildren()
	{
		// Loop through child components
		final Iterator<? extends Component> iter = iterator();
		while (iter.hasNext())
		{
			// Get next child
			final Component child = iter.next();

			// Call end request on the child
			child.afterRender();
		}
		super.onAfterRenderChildren();
	}
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		tag.put("xmlns:wicket", "http://wicket.apache.org");
	}
