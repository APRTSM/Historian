	private String internalGetMinifiedName()
	{
		String minifiedName = MINIFIED_NAMES_CACHE.get(this);
		if (minifiedName != null)
		{
			return minifiedName;
		}

		String name = getMinifiedName();
		IResourceStreamLocator locator = Application.get()
				.getResourceSettings()
				.getResourceStreamLocator();
		String absolutePath = Packages.absolutePath(getScope(), name);
		IResourceStream stream = locator.locate(getScope(), absolutePath, getStyle(),
				getVariation(), getLocale(), null, true);

		minifiedName = stream != null ? name : NO_MINIFIED_NAME;
		MINIFIED_NAMES_CACHE.put(this, minifiedName);
		if (minifiedName == NO_MINIFIED_NAME && log.isDebugEnabled())
		{
			log.debug("No minified version of '" + super.getName() +
					"' found, expected a file with the name '" + name + "', using full version");
		}
		return minifiedName;
	}
