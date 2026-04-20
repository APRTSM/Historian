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
		String valueTypeName = (value != null ? value.getClass().getName() : "null");
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectOutputStream(out).writeObject(value);
			log.debug("Stored attribute " + name + "{ " + valueTypeName + "} with size: "
					+ Bytes.bytes(out.size()));
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(
					"Internal error cloning object. Make sure all dependent objects implement Serializable. Class: "
							+ valueTypeName, e);
		}

		// Set the actual attribute
		store.setAttribute(request, name, value);
	}
