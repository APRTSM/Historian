	public void scheduleRequestHandlerAfterCurrent(IRequestHandler handler)
	{
		// just delegating the call to {@link IRequestHandlerExecutor} and invoking listeners
		requestHandlerExecutor.schedule(handler);

		// only forward calls to the listeners when handler is null
		if (handler != null)
			listeners.onRequestHandlerScheduled(handler);
	}
