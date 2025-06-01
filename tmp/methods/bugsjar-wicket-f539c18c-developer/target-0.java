	private Property getProperty()
	{
		if (property_ == null)
		{
			property_ = BeanValidationConfiguration.get().resolveProperty(component);
			if (property_ == null)
			{
				throw new IllegalStateException(
					"Could not resolve Property from component: " + component
						+ ". Either specify the Property in the constructor or use a model that works in combination with a "
						+ IPropertyResolver.class.getSimpleName()
						+ " to resolve the Property automatically");
			}
		}
		return property_;
	}
	boolean isRequired()
	{
		List<NotNull> constraints = findNotNullConstraints();

		if (constraints.isEmpty())
		{
			return false;
		}

		HashSet<Class<?>> validatorGroups = new HashSet<Class<?>>();
		validatorGroups.addAll(Arrays.asList(getGroups()));

		for (NotNull constraint : constraints)
		{
			if (canApplyToDefaultGroup(constraint) && validatorGroups.isEmpty())
			{
				return true;
			}

			for (Class<?> constraintGroup : constraint.groups())
			{
				if (validatorGroups.contains(constraintGroup))
				{
					return true;
				}
			}
		}

		return false;
	}
	private boolean canApplyToDefaultGroup(NotNull constraint)
	{
		List<Class<?>> groups = Arrays.asList(constraint.groups());
		//the constraint can be applied to default group either if its group array is empty
		//or if it contains javax.validation.groups.Default
		return groups.size() == 0 || groups.contains(Default.class);
	}
	public void bind(Component component)
	{
		if (this.component != null)
		{
			throw new IllegalStateException( //
				"This validator has already been added to component: "
					+ this.component
					+ ". This validator does not support reusing instances, please create a new one");
		}

		if (!(component instanceof FormComponent))
		{
			throw new IllegalStateException(getClass().getSimpleName()
				+ " can only be added to FormComponents");
		}

		// TODO add a validation key that appends the type so we can have
		// different messages for
		// @Size on String vs Collection - done but need to add a key for each
		// superclass/interface

		this.component = (FormComponent<T>)component;
	}
