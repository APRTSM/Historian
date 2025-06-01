	private final void internalBeforeRender()
	{
		configure();

		// check authorization
		setRenderAllowed();

		if ((determineVisibility()) && !getFlag(FLAG_RENDERING) &&
			!getFlag(FLAG_PREPARED_FOR_RENDER))
		{
			setRequestFlag(RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED, false);

			getApplication().getComponentPreOnBeforeRenderListeners().onBeforeRender(this);

			onBeforeRender();
			getApplication().getComponentPostOnBeforeRenderListeners().onBeforeRender(this);

			if (!getRequestFlag(RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED))
			{
				throw new IllegalStateException(Component.class.getName() +
					" has not been properly rendered. Something in the hierarchy of " +
					getClass().getName() +
					" has not called super.onBeforeRender() in the override of onBeforeRender() method");
			}
		}
	}
	public void internalPrepareForRender(boolean setRenderingFlag)
	{
		beforeRender();

		if (setRenderingFlag)
		{
			// only process feedback panel when we are about to be rendered.
			// setRenderingFlag is false in case prepareForRender is called only to build component
			// hierarchy (i.e. in BookmarkableListenerInterfaceRequestTarget).
			// prepareForRender(true) is always called before the actual rendering is done so
			// that's where feedback panels gather the messages

			List<Component> feedbacks = getRequestCycle().getMetaData(FEEDBACK_LIST);
			if (feedbacks != null)
			{
				for (Component feedback : feedbacks)
				{
					feedback.internalBeforeRender();
				}
			}
			getRequestCycle().setMetaData(FEEDBACK_LIST, null);
		}

		markRendering(setRenderingFlag);
	}
