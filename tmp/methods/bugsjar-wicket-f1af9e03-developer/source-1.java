	public Component(final String id, final IModel<?> model)
	{
		setId(id);
		getApplication().getComponentInstantiationListeners().onInstantiation(this);

		final DebugSettings debugSettings = getApplication().getDebugSettings();
		if (debugSettings.isLinePreciseReportingOnNewComponentEnabled() && debugSettings.getComponentUseCheck())
		{
			setMetaData(CONSTRUCTED_AT_KEY,
				ComponentStrings.toString(this, new MarkupException("constructed")));
		}

		if (model != null)
		{
			setModelImpl(wrap(model));
		}
	}
	private Page(final PageParameters parameters, IModel<?> model)
	{
		super(null, model);

		if (parameters == null)
		{
			pageParameters = new PageParameters();
		}
		else
		{
			pageParameters = parameters;
		}
		init();
	}
	private void init()
	{
		if (isBookmarkable() == false)
		{
			setStatelessHint(false);
		}

		// Set versioning of page based on default
		setVersioned(getApplication().getPageSettings().getVersionPagesByDefault());

		// All Pages are born dirty so they get clustered right away
		dirty(true);

		// this is a bit of a dirty hack, but calling dirty(true) results in isStateless called
		// which is bound to set the stateless cache to true as there are no components yet
		stateless = null;
	}
