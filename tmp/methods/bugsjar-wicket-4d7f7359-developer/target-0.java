	public final boolean isVisibleInHierarchy()
	{
		Component parent = getParent();
		if (parent != null && !parent.isVisibleInHierarchy())
		{
			return false;
		}
		else
		{
			return determineVisibility();
		}
	}
