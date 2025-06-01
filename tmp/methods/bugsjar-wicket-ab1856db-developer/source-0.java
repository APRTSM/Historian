	public IRequestHandler onException(final RequestCycle cycle, final Exception ex)
	{
		final List<IRequestHandler> handlers = new ArrayList<IRequestHandler>();

		notify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				IRequestHandler handler = listener.onException(cycle, ex);
				if (handler != null)
				{
					handlers.add(handler);
				}
			}
		});

		if (handlers.isEmpty())
		{
			return null;
		}

		if (handlers.size() > 1)
		{
			throw new WicketRuntimeException(
				"More than one request cycle listener returned a request handler while handling the exception.",
				ex);
		}

		return handlers.get(0);
	}
