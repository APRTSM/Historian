	protected final void onEvent(final AjaxRequestTarget target)
	{
		final FormComponent<?> formComponent = getFormComponent();

		if ("blur".equals(getEvent().toLowerCase()) && disableFocusOnBlur())
		{
			target.focusComponent(null);
		}

		try
		{
			formComponent.inputChanged();
			formComponent.validate();
			if (formComponent.isValid())
			{
				formComponent.valid();
				if (getUpdateModel())
				{
					formComponent.updateModel();
				}

				onUpdate(target);
			}
			else
			{
				formComponent.invalid();

				onError(target, null);
			}
		}
		catch (RuntimeException e)
		{
			onError(target, e);

		}
	}
	protected void onBind()
	{
		super.onBind();

		Component component = getComponent();
		if (!(component instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
				+ " can only be added to an instance of a FormComponent");
		}

		checkComponent((FormComponent<?>)component);
	}
	protected void checkComponent(FormComponent<?> component)
	{
		if (Application.get().usesDevelopmentConfig()
			&& AjaxFormChoiceComponentUpdatingBehavior.appliesTo(component))
		{
			log.warn(String
				.format(
					"AjaxFormComponentUpdatingBehavior is not supposed to be added in the form component at path: \"%s\". "
						+ "Use the AjaxFormChoiceComponentUpdatingBehavior instead, that is meant for choices/groups that are not one component in the html but many",
					component.getPageRelativePath()));
		}
	}
