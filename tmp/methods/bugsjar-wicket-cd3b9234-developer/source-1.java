	private boolean anyFormComponentError()
	{
		// Check ALL children for error messages irrespective of FormComponents or not
		Boolean error = visitChildren(Component.class, new IVisitor<Component, Boolean>()
		{
			@Override
			public void component(final Component component, final IVisit<Boolean> visit)
			{
				if (component.hasErrorMessage())
				{
					visit.stop(true);
				}
			}
		});

		return (error != null) && error;
	}
