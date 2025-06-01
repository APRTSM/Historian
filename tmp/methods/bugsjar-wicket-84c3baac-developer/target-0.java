	protected final void validateValidators()
	{
		final IValidatable<T> validatable = newValidatable();

		boolean isNull = getConvertedInput() == null;

		IValidator<T> validator = null;

		try
		{
			for (Behavior behavior : getBehaviors())
			{
				validator = null;
				if (behavior instanceof ValidatorAdapter)
				{
					validator = ((ValidatorAdapter<T>)behavior).getValidator();
				}
				else if (behavior instanceof IValidator)
				{
					validator = (IValidator<T>)behavior;
				}
				if (validator != null)
				{
					if (isNull == false || validator instanceof INullAcceptingValidator<?>)
					{
						validator.validate(validatable);
					}
					if (!isValid())
					{
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Exception '" + e + "' occurred during validation " +
				validator.getClass().getName() + " on component " + getPath(), e);
		}
	}
