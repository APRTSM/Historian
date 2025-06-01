	public void validate(IValidatable<V> validatable)
	{
		R value = getValue(validatable);
		final R min = getMinimum();
		final R max = getMaximum();
		if ((min != null && value.compareTo(min) < 0) || (max != null && value.compareTo(max) > 0))
		{
			ValidationError error = new ValidationError(this, getMode().getVariation());
			if (min != null)
			{
				error.setVariable("minimum", min);
			}
			if (max != null)
			{
				error.setVariable("maximum", max);
			}
			validatable.error(decorate(error, validatable));
		}
	}
