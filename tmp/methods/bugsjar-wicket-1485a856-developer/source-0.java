	public void process(IFormSubmitter submittingComponent)
	{
		// save the page in case the component is removed during submit
		final Page page = getPage();
		String hiddenFieldId = getHiddenFieldId();

		if (!isEnabledInHierarchy() || !isVisibleInHierarchy())
		{
			// since process() can be called outside of the default form workflow, an additional
			// check is needed

			// FIXME throw listener exception
			return;
		}

		// run validation
		validate();

		// If a validation error occurred
		if (hasError())
		{
			// mark all children as invalid
			markFormComponentsInvalid();

			// let subclass handle error
			callOnError(submittingComponent);
		}
		else
		{
			// mark all children as valid
			markFormComponentsValid();

			// before updating, call the interception method for clients
			beforeUpdateFormComponentModels();

			// Update model using form data
			updateFormComponentModels();

			// validate model objects after input values have been bound
			onValidateModelObjects();
			if (hasError())
			{
				callOnError(submittingComponent);
				return;
			}

			// Form has no error
			delegateSubmit(submittingComponent);
		}
	}
