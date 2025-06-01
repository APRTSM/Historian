	protected Class<? extends IRequestablePage> getPageClass(String name)
	{
		Args.notEmpty(name, "name");

		return WicketObjects.resolveClass(name);
	}
