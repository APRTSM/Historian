	boolean isSelected(final SelectOption<?> option)
	{
		Args.notNull(option, "option");

		// if the raw input is specified use that, otherwise use model
		if (hasRawInput())
		{
			String[] paths = getInputAsArray();
			if ((paths != null) && (paths.length > 0))
			{
				for (String path : paths)
				{
					if (path.equals(option.getPath()))
					{
						return true;
					}
				}
				return false;
			}
		}

		return compareModels(getDefaultModelObject(), option.getDefaultModelObject());
	}
