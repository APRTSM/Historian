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
