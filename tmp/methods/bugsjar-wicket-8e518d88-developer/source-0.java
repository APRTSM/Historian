	public void onInstantiation(Component component)
	{
		Class<? extends Component> componentClass = component.getClass();

		if (componentClass.isMemberClass() && Modifier.isStatic(componentClass.getModifiers()) == false)
		{
			LOG.debug("Skipping non-static inner class '{}' ", componentClass);
		}
		else
		{
			inject(component);
		}
	}
