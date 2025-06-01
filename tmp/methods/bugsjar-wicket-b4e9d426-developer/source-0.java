	public void invalidate(Request request)
	{
		for (UnboundListener l : unboundListeners)
		{
			l.sessionUnbound(sessionId);
		}

		cleanup();
	}
