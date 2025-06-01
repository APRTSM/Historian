	public final void afterRender()
	{
		// if the component has been previously attached via attach()
		// detach it now
		try
		{
			setFlag(FLAG_AFTER_RENDERING, true);
			onAfterRender();
			getApplication().getComponentOnAfterRenderListeners().onAfterRender(this);
			if (getFlag(FLAG_AFTER_RENDERING))
			{
				throw new IllegalStateException(Component.class.getName() +
					" has not been properly detached. Something in the hierarchy of " +
					getClass().getName() +
					" has not called super.onAfterRender() in the override of onAfterRender() method");
			}
			// always detach children because components can be attached
			// independently of their parents
			onAfterRenderChildren();
		}
		finally
		{
			// this flag must always be set to false.
			setFlag(FLAG_RENDERING, false);
		}
	}
	public Component setMarkupId(String markupId)
	{
		if (markupId != null && Strings.isEmpty(markupId))
		{
			throw new IllegalArgumentException("Markup id cannot be an empty string");
		}

		// TODO check if an automatic id has already been generated or getmarkupid() called
		// previously and throw an illegalstateexception because something else might be depending
		// on previous id

		setMarkupIdImpl(markupId);
		return this;
	}
	void internalMarkRendering(boolean setRenderingFlag)
	{
		if (setRenderingFlag)
		{
			setFlag(FLAG_PREPARED_FOR_RENDER, false);
			setFlag(FLAG_RENDERING, true);
		}
	}
