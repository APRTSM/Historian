	public final void detach()
	{
		super.detach();

		// detach any model
		if (model != null)
		{
			model.detach();
		}

		// some parameters can be detachable
		if (parameters != null)
		{
			for (Object parameter : parameters)
			{
				if (parameter instanceof IDetachable)
				{
					((IDetachable)parameter).detach();
				}
			}
		}

		if (defaultValue != null)
		{
			defaultValue.detach();
		}
	}
