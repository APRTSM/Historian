	public void scheduleRequestHandlerAfterCurrent(IRequestHandler handler)
	{
		// just delegating the call to {@link IRequestHandlerExecutor} and invoking listeners
		requestHandlerExecutor.schedule(handler);
		listeners.onRequestHandlerScheduled(handler);
	}
