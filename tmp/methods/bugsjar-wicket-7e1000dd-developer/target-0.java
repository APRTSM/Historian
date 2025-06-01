	protected final void setAttribute(String name, Object value)
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle == null)
		{
			throw new WicketRuntimeException("Can not set the attribute. No RequestCycle available");
		}

		ISessionStore store = getSessionStore();
		Request request = cycle.getRequest();

		// extra check on session binding event
		if (value == this)
		{
			Object current = store.getAttribute(request, name);
			if (current == null)
			{
				String id = store.getSessionId(request, false);
				if (id != null)
				{
					// this is a new instance. wherever it came from, bind the
					// session now
					store.bind(request, (Session)value);
				}
			}
		}

		// Set the actual attribute
		store.setAttribute(request, name, value);
	}
