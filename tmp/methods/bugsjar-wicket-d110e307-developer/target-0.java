	public Property resolveProperty(FormComponent<?> component)
	{
		IModel<?> model = component.getModel();

		while (true)
		{
			if (model == null)
			{
				return null;
			}
			if (model instanceof IPropertyReflectionAwareModel)
			{
				break;
			}
			if (model instanceof IWrapModel<?>)
			{
				model = ((IWrapModel<?>)model).getWrappedModel();
				continue;
			}
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
			String name = getter.getName().substring(3, 4).toLowerCase() +
				getter.getName().substring(4);
			return new Property(getter.getDeclaringClass(), name);
		}

		return null;
	}
