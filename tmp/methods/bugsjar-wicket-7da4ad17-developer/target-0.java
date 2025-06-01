	protected String resourceKey(T object)
	{
		return object.getDeclaringClass().getSimpleName() + "." + object.name();
	}
