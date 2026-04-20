	public final void detach()
	{
		// if the component has been previously attached via attach()
		// detach it now
		setFlag(FLAG_DETACHING, true);
		onDetach();
		if (getFlag(FLAG_DETACHING))
		{
			throw new IllegalStateException(Component.class.getName() +
				" has not been properly detached. Something in the hierarchy of " +
				getClass().getName() +
				" has not called super.onDetach() in the override of onDetach() method");
		}

		// always detach models because they can be attached without the
		// component. eg component has a compoundpropertymodel and one of its
		// children component's getmodelobject is called
		detachModels();

		// detach any behaviors
		new Behaviors(this).detach();

		// always detach children because components can be attached
		// independently of their parents
		detachChildren();

		// reset the model to null when the current model is a IWrapModel and
		// the model that created it/wrapped in it is a IComponentInheritedModel
		// The model will be created next time.
		if (getFlag(FLAG_INHERITABLE_MODEL))
		{
			setModelImpl(null);
			setFlag(FLAG_INHERITABLE_MODEL, false);
		}

		clearEnabledInHierarchyCache();
		clearVisibleInHierarchyCache();

		requestFlags = 0;

		internalDetach();

		// notify any detach listener
		IDetachListener detachListener = getApplication().getFrameworkSettings()
			.getDetachListener();
		if (detachListener != null)
		{
			detachListener.onDetach(this);
		}
	}
	private void internalDetach()
	{
		markup = null;
	}
	public final Component setOutputMarkupPlaceholderTag(final boolean outputTag)
	{
		if (outputTag != getFlag(FLAG_PLACEHOLDER))
		{
			if (outputTag)
			{
				setOutputMarkupId(true);
				setFlag(FLAG_PLACEHOLDER, true);
			}
			else
			{
				setFlag(FLAG_PLACEHOLDER, false);
				// I think it's better to not setOutputMarkupId to false...
				// user can do it if she want
			}
		}
		return this;
	}
	public InlineEnclosure(final String id, final String childId)
	{
		super(id, childId);

		enclosureMarkupAsString = null;

		// ensure that the Enclosure is ready for ajax updates
		setOutputMarkupPlaceholderTag(true);
		setMarkupId(getId());
	}
	public IMarkupFragment getMarkup()
	{
		IMarkupFragment enclosureMarkup = null;
		if (enclosureMarkupAsString == null)
		{
			IMarkupFragment markup = super.getMarkup();
			if (markup != null && markup != Markup.NO_MARKUP)
			{
				enclosureMarkup = markup;
				enclosureMarkupAsString = markup.toString(true);
			}
		}
		else
		{
			enclosureMarkup = Markup.of(enclosureMarkupAsString);
		}

		return enclosureMarkup;
	}
