	public String toString()
	{
	 StringBuilder sb = new StringBuilder(super.toString());
		sb.append(":attached=").append(attached).append(":tempModelObject=[").append(
			this.transientModelObject).append("]");
		return sb.toString();
	}
	public void detach()
	{
		if (attached)
		{
			try
			{
				onDetach();
			}
			finally
			{
				attached = false;
				transientModelObject = null;

				log.debug("removed transient object for {}, requestCycle {}", this,
					RequestCycle.get());
			}
		}
	}
	public final T getObject()
	{
		if (!attached)
		{
			transientModelObject = load();

			if (log.isDebugEnabled())
			{
				log.debug("loaded transient object " + transientModelObject + " for " + this +
					", requestCycle " + RequestCycle.get());
			}

			attached = true;
			onAttach();
		}
		return transientModelObject;
	}
	public final boolean isAttached()
	{
		return attached;
	}
	public LoadableDetachableModel(T object)
	{
		this.transientModelObject = object;
		attached = true;
	}
	public void setObject(final T object)
	{
		attached = true;
		transientModelObject = object;
	}
