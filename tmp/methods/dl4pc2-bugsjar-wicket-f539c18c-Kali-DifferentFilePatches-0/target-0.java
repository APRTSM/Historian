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
			if (true)
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
