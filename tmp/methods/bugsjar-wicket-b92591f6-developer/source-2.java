	private void dequeueAutoComponents()
	{
		// dequeue auto components
		DequeueContext context = newDequeueContext();
		if (context != null && context.peekTag() != null)
		{
			for (ComponentTag tag = context.takeTag(); tag != null; tag = context.takeTag())
			{
				ComponentTag.IAutoComponentFactory autoComponentFactory = tag.getAutoComponentFactory();
				if (autoComponentFactory != null)
				{
					queue(autoComponentFactory.newComponent(tag));
				}
			}
		}
	}
