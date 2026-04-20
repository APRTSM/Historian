	public void setObject(final T object)
	{
		attached = AttachingState.ATTACHED;
		transientModelObject = object;
	}
	public void detach()
	{
		if (attached == AttachingState.ATTACHED)
		{
			try
			{
				onDetach();
			}
			finally
			{
				attached = AttachingState.DETACHED;
				transientModelObject = null;

				log.debug("removed transient object for {}, requestCycle {}", this,
					RequestCycle.get());
			}
		}
	}
	public final boolean isAttached()
	{
		return attached.isAttached();
	}
	public final T getObject()
	{
		if (attached == AttachingState.DETACHED)
		{
			// prevent infinite attachment loops
			attached = AttachingState.ATTACHING;

			transientModelObject = load();

			if (log.isDebugEnabled())
			{
				log.debug("loaded transient object " + transientModelObject + " for " + this +
					", requestCycle " + RequestCycle.get());
			}

			attached = AttachingState.ATTACHED;
			onAttach();
		}
		return transientModelObject;
	}
	public LoadableDetachableModel(T object)
	{
		this.transientModelObject = object;
		attached = AttachingState.ATTACHED;
	}
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(":attached=")
			.append(isAttached())
			.append(":tempModelObject=[")
			.append(this.transientModelObject)
			.append("]");
		return sb.toString();
	}
