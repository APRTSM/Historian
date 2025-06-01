	public void validate(IValidatable<V> validatable)
	{
		R value = getValue(validatable);
		final R min = getMinimum();
		final R max = getMaximum();
		if ((min != null && value.compareTo(min) < 0) || (max != null && value.compareTo(max) > 0))
		{
			Mode mode = getMode();
			ValidationError error = new ValidationError(this, mode.getVariation());
			if (min != null)
			{
				error.setVariable("minimum", min);
			}
			if (max != null)
			{
				error.setVariable("maximum", max);
			}
			if (mode == Mode.EXACT)
			{
				error.setVariable("exact", max);
			}
			validatable.error(decorate(error, validatable));
		}
	}
