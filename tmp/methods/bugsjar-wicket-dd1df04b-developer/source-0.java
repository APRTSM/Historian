	protected void callOnError(IFormSubmitter submitter)
	{
		if (submitter != null)
		{
			submitter.onError();
		}
		onError();
		// call onError on nested forms
		visitChildren(Form.class, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				final Form<?> form = (Form<?>)component;
				if (!form.isEnabledInHierarchy() || !form.isVisibleInHierarchy())
				{
					visit.dontGoDeeper();
					return;
				}
				if (form.hasError())
				{
					form.onError();
				}
			}
		});
	}
	protected void delegateSubmit(IFormSubmitter submittingComponent)
	{
		final Form<?> processingForm = findFormToProcess(submittingComponent);


		// process submitting component (if specified)
		if (submittingComponent != null)
		{
			// invoke submit on component
			submittingComponent.onSubmitBeforeForm();
		}

		// invoke Form#onSubmit(..) going from innermost to outermost
		Visits.visitPostOrder(processingForm, new IVisitor<Form<?>, Void>()
		{
			@Override
			public void component(Form<?> form, IVisit<Void> visit)
			{
				if (form.isEnabledInHierarchy() && form.isVisibleInHierarchy())
				{
					form.onSubmit();
				}
			}
		}, new ClassVisitFilter(Form.class));

		if (submittingComponent != null)
		{
			submittingComponent.onSubmitAfterForm();
		}
	}
