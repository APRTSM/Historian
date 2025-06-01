	public final boolean isStateless()
	{
		if (!getStatelessHint())
		{
			return false;
		}

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

		for (Behavior behavior : getBehaviors())
		{
			if (!behavior.getStatelessHint(this))
			{
				return false;
			}
		}
		return true;
	}
