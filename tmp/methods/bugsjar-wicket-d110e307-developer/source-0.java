	public Property resolveProperty(FormComponent<?> component)
	{
		IModel<?> model = component.getModel();

		if (!(model instanceof IPropertyReflectionAwareModel))
		{
			return null;
		}

		IPropertyReflectionAwareModel<?> delegate = (IPropertyReflectionAwareModel<?>)model;

		Field field = delegate.getPropertyField();
		if (field != null)
		{
			return new Property(field.getDeclaringClass(), field.getName());
		}
	
		Method getter = delegate.getPropertyGetter();
		if (getter != null)
		{
			String name = getter.getName().substring(3, 1).toLowerCase() +
				getter.getName().substring(4);
			return new Property(getter.getDeclaringClass(), name);
		}

		return null;
	}
