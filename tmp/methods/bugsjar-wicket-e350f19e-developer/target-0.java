	private List<NotNull> findNotNullConstraints()
	{
		BeanValidationContext config = BeanValidationConfiguration.get();
		Validator validator = config.getValidator();
		Property property = getProperty();

		List<NotNull> constraints = new ArrayList<NotNull>();

		Iterator<ConstraintDescriptor<?>> it = new ConstraintIterator(validator, property);

		while (it.hasNext())
		{
			ConstraintDescriptor<?> desc = it.next();
			if (desc.getAnnotation().annotationType().equals(NotNull.class))
			{
				constraints.add((NotNull)desc.getAnnotation());
			}
		}

		return constraints;
	}
	public void onConfigure(Component component)
	{
		super.onConfigure(component);
		if (requiredFlagSet == false)
		{
			// "Required" flag is calculated upon component's model property, so
			// we must ensure,
			// that model object is accessible (i.e. component is already added
			// in a page).
			requiredFlagSet = true;
			if (isRequired())
			{
				this.component.setRequired(true);
			}
		}
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
			if (constraint.groups().length == 0 && validatorGroups.isEmpty())
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
	public void bind(Component component)
	{
		if (this.component != null)
		{
			throw new IllegalStateException( //
				"This validator has already been added to component: " + this.component +
					". This validator does not support reusing instances, please create a new one");
		}

		if (!(component instanceof FormComponent))
		{
			throw new IllegalStateException(getClass().getSimpleName() +
				" can only be added to FormComponents");
		}

		// TODO add a validation key that appends the type so we can have
		// different messages for
		// @Size on String vs Collection - done but need to add a key for each
		// superclass/interface

		this.component = (FormComponent<T>)component;
	}
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);

		BeanValidationContext config = BeanValidationConfiguration.get();
		Validator validator = config.getValidator();
		Property property = getProperty();

		// find any tag modifiers that apply to the constraints of the property
		// being validated
		// and allow them to modify the component tag

		Iterator<ConstraintDescriptor<?>> it = new ConstraintIterator(validator, property,
			getGroups());

		while (it.hasNext())
		{
			ConstraintDescriptor<?> desc = it.next();

			ITagModifier modifier = config.getTagModifier(desc.getAnnotation().annotationType());

			if (modifier != null)
			{
				modifier.modify((FormComponent<?>)component, tag, desc.getAnnotation());
			}
		}
	}
