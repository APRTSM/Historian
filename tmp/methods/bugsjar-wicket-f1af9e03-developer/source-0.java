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
