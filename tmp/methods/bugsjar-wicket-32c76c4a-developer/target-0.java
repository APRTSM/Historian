	boolean isSelected(final SelectOption<?> option)
	{
		Args.notNull(option, "option");

		// if the raw input is specified use that, otherwise use model
		if (hasRawInput())
		{
			String[] values = getInputAsArray();
			if (values != null && values.length > 0)
			{
				for (int i = 0; i < values.length; i++)
				{
					String value = values[i];
					if (value.equals(option.getValue()))
					{
						return true;
					}
				}
				return false;
			}
		}

		return compareModels(getDefaultModelObject(), option.getDefaultModelObject());
	}
