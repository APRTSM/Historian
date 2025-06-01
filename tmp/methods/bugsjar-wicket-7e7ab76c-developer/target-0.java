	public final FormComponent<T> setConvertEmptyInputStringToNull(boolean flag)
	{
		setFlag(FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL, flag);
		return this;
	}
	private void resolveType()
	{
		if (!getFlag(TYPE_RESOLVED) && getType() == null)
		{
			Class<?> type = getModelType(getDefaultModel());
			setType(type);
			setFlag(TYPE_RESOLVED, true);
		}
	}
	protected void convertInput()
	{
		// Stateless forms don't have to be rendered first, convertInput could be called before
		// onBeforeRender calling resolve type here again to check if the type is correctly set.
		resolveType();
		String[] value = getInputAsArray();
		String tmp = value != null && value.length > 0 ? value[0] : null;
		if (getConvertEmptyInputStringToNull() && Strings.isEmpty(tmp))
		{
			setConvertedInput(null);
		}
		else
		{
			super.convertInput();
		}
	}
