	protected void init(final IWizardModel wizardModel)
	{
		if (wizardModel == null)
		{
			throw new IllegalArgumentException("argument wizardModel must be not null");
		}

		this.wizardModel = wizardModel;

		form = newForm(FORM_ID);
		addOrReplace(form);
		// dummy view to be replaced
		form.addOrReplace(new WebMarkupContainer(HEADER_ID));
		form.addOrReplace(newFeedbackPanel(FEEDBACK_ID));
		// add dummy view; will be replaced on initialization
		form.addOrReplace(new WebMarkupContainer(VIEW_ID));
		form.addOrReplace(newButtonBar(BUTTONS_ID));
		form.addOrReplace(newOverviewBar(OVERVIEW_ID));

		wizardModel.addListener(this);

		// reset model to prepare for action
		wizardModel.reset();
	}
