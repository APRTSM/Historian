	public Class<T> getObjectClass()
	{
		final String expression = propertyExpression();
		if (Strings.isEmpty(expression))
		{
			// Return a meaningful value for an empty property expression
			Object target = getTarget();
			return (Class<T>)(target != null ? target.getClass() : null);
		}

		final Object target = getTarget();
		if (target != null)
		{
			try
			{
				return (Class<T>)PropertyResolver.getPropertyClass(expression, target);
			}
			catch (Exception e)
			{
				// ignore.
			}
		}
		else if (this.target instanceof IObjectClassAwareModel)
		{
			try
			{
				return PropertyResolver.getPropertyClass(expression,
					((IObjectClassAwareModel<?>)this.target).getObjectClass());
			}
			catch (WicketRuntimeException e)
			{
				// it was just a try.
			}

		}
		return null;
	}
