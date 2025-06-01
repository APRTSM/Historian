	protected String resourceKey(T object)
	{
		return object.getClass().getSimpleName() + "." + object.name();
	}
