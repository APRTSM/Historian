	void setModelImpl(IModel<?> model)
	{
		if (getFlag(FLAG_MODEL_SET))
		{
			if (model != null)
			{
				data_set(0, model);
				// WICKET-3413 reset 'inherited model' flag if model changed
				// and a new one is not IComponentInheritedModel
				if (getFlag(FLAG_INHERITABLE_MODEL) && !(model instanceof IComponentInheritedModel))
				{
					setFlag(FLAG_INHERITABLE_MODEL, false);
				}
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
