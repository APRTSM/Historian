	public Component setDefaultModel(final IModel<?> model)
	{
		IModel<?> prevModel = getModelImpl();
		// Detach current model
		if (prevModel != null)
		{
			prevModel.detach();
		}

		IModel<?> wrappedModel = prevModel;
		if (prevModel instanceof IWrapModel)
		{
			wrappedModel = ((IWrapModel<?>)prevModel).getWrappedModel();
		}

		// Change model
		if (wrappedModel != model)
		{
			if (wrappedModel != null)
			{
				addStateChange();
			}

			setModelImpl(wrap(model));
		}

		modelChanged();
		return this;
	}
