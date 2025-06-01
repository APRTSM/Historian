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
			String resource = loadStringResource(validator.getClass(), key, locale, style,
				variation);
			if (resource != null)
			{
				return resource;
			}
		}

		// not found
		return null;
	}
