	protected T convertValue(String[] value) throws ConversionException
	{
		String tmp = value != null && value.length > 0 ? value[0] : null;
		if (getConvertEmptyInputStringToNull() && Strings.isEmpty(tmp))
		{
			return null;
		}
		return super.convertValue(value);
	}
	private void resolveType()
	{
		if (!getFlag(TYPE_RESOLVED) && getType() == null)
		{
			// Set the type, but only if it's not a String (see WICKET-606).
			// Otherwise, getConvertEmptyInputStringToNull() won't work.
			Class<?> type = getModelType(getDefaultModel());
			if (!String.class.equals(type))
			{
				setType(type);
			}
			setFlag(TYPE_RESOLVED, true);
		}
	}
	protected void convertInput()
	{
		// Stateless forms don't have to be rendered first, convertInput could be called before
		// onBeforeRender calling resolve type here again to check if the type is correctly set.
		resolveType();
		super.convertInput();
	}
