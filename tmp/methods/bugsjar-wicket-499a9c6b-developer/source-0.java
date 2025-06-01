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
