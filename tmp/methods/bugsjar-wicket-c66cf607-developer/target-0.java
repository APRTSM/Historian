	public final boolean isStateless()
	{
		if (
			// the component is either invisible or disabled
			(isVisibleInHierarchy() && isEnabledInHierarchy()) == false &&

			// and it can't call listener interfaces
			canCallListenerInterface(null) == false
		)
		{
			// then pretend the component is stateless
			return true;
		}

		if (!getStatelessHint())
		{
			return false;
		}

		for (Behavior behavior : getBehaviors())
		{
			if (!behavior.getStatelessHint(this))
			{
				return false;
			}
		}
		return true;
	}
