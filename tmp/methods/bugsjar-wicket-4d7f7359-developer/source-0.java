	public final boolean isVisibleInHierarchy()
	{
		Boolean state = getMetaData(VISIBLE_IN_HIERARCHY_CACHE_KEY);
		if (state == null)
		{
			Component parent = getParent();
			if (parent != null && !parent.isVisibleInHierarchy())
			{
				state = false;
			}
			else
			{
				state = determineVisibility();
			}
			setMetaData(VISIBLE_IN_HIERARCHY_CACHE_KEY, state);
		}
		return state;
	}
