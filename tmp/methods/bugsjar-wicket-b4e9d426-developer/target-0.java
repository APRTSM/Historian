	public void invalidate(Request request)
	{
		String sessId = sessionId;
		cleanup();
		for (UnboundListener l : unboundListeners)
		{
			l.sessionUnbound(sessId);
		}

	}
