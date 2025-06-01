	protected void onAfterRenderChildren()
	{
		// Loop through child components
		for (Component child : this)
		{
			// Call end request on the child
			child.afterRender();
		}

		super.onAfterRenderChildren();
	}
