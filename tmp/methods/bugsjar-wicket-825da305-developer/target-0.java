	protected Class<? extends IRequestablePage> getPageClass(String name)
	{
		String cleanedClassName = cleanClassName(name);
		return WicketObjects.resolveClass(cleanedClassName);
	}
	protected String cleanClassName(String className)
	{
		Args.notEmpty(className, "className");

		if (Strings.indexOf(className, ';') > -1)
		{
			// remove any path parameters set manually by the user. WICKET-5500
			className = Strings.beforeFirst(className, ';');
		}

		return className;
	}
