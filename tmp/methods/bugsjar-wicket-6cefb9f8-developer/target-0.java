	private void internalAdd(final Behavior behavior)
	{
		component.data_add(behavior);
		if (behavior.getStatelessHint(component) == false)
		{
			getBehaviorId(behavior);
		}
	}
