	void setModelImpl(IModel<?> model)
	{
		if (getFlag(FLAG_MODEL_SET))
		{
			if (model != null)
			{
				data_set(0, model);
			}
			else
			{
				data_remove(0);
				setFlag(FLAG_MODEL_SET, false);
			}
		}
		else
		{
			if (model != null)
			{
				data_insert(0, model);
				setFlag(FLAG_MODEL_SET, true);
			}
		}
	}
	public Component setDefaultModel(final IModel<?> model)
	{
		IModel<?> prevModel = getModelImpl();

		IModel<?> wrappedModel = prevModel;
		if (prevModel instanceof IWrapModel)
		{
			wrappedModel = ((IWrapModel<?>)prevModel).getWrappedModel();
		}

		// Change model
		if (wrappedModel != model)
		{
			// Detach the old/current model
			if (prevModel != null)
			{
				prevModel.detach();
			}

			modelChanging();
			setModelImpl(wrap(model));
			modelChanged();

			// WICKET-3413 reset 'inherited model' when model is explicitely set
			setFlag(FLAG_INHERITABLE_MODEL, false);
		}

		return this;
	}
