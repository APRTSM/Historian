	public String loadStringResource(final Component component, final String key,
		final Locale locale, final String style, final String variation)
	{
		if (component == null || !(component instanceof FormComponent))
		{
			return null;
		}

		FormComponent<?> fc = (FormComponent<?>)component;
		for (IValidator<?> validator : fc.getValidators())
		{
			Class<?> scope = getScope(validator);
			String resource = loadStringResource(scope, key, locale, style,
				variation);
			if (resource != null)
			{
				return resource;
			}
		}

		// not found
		return null;
	}
	private Class<? extends IValidator> getScope(IValidator<?> validator)
	{
		Class<? extends IValidator> scope;
		if (validator instanceof ValidatorAdapter)
		{
			scope = ((ValidatorAdapter) validator).getValidator().getClass();
		}
		else
		{
			scope = validator.getClass();
		}
		return scope;
	}
