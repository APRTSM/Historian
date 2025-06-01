	private void internalAdd(final Behavior behavior)
	{
		component.data_add(behavior);
		if (behavior.isStateless(component))
		{
			getBehaviorId(behavior);
		}
	}
	public boolean isStateless(Component component)
	{
		return false;
	}
