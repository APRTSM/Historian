	public final void invoke(final IRequestableComponent component, final IBehavior behavior)
	{
		if (!component.canCallListenerInterface())
		{
			// just return so that we have a silent fail and just re-render the page
			log.warn("component not enabled or visible; ignoring call. Component: " + component);
			return;
		}

		// XXX a bit of an ugly cast here from IRequestableComponent to Component
		if (!behavior.isEnabled((Component)component))
		{
			log.warn("behavior not enabled; ignore call. Behavior {} at component {}", behavior,
				component);
		}

		try
		{
			// Invoke the interface method on the component
			method.invoke(behavior, new Object[] { });
		}
		catch (InvocationTargetException e)
		{
			if (e.getTargetException() instanceof ReplaceHandlerException ||
				e.getTargetException() instanceof AuthorizationException ||
				e.getTargetException() instanceof WicketRuntimeException)
			{
				throw (RuntimeException)e.getTargetException();
			}
			throw new WicketRuntimeException("Method " + method.getName() + " of " +
				method.getDeclaringClass() + " targeted at behavior " + behavior +
				" on component " + component + " threw an exception", e);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Method " + method.getName() + " of " +
				method.getDeclaringClass() + " targeted at behavior " + behavior +
				" on component " + component + " threw an exception", e);
		}
	}
