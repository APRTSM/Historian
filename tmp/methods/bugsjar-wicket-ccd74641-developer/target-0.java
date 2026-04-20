	public IRequestHandler map(Exception e)
	{
		try
		{
			Response response = RequestCycle.get().getResponse();
			if (response instanceof WebResponse)
			{
				// we don't wan't to cache an exceptional reply in the browser
				((WebResponse)response).disableCaching();
			}
			return internalMap(e);
		}
		catch (RuntimeException e2)
		{
			if (logger.isDebugEnabled())
			{
				logger.error(
					"An error occurred while handling a previous error: " + e2.getMessage(), e2);
			}

			// hmmm, we were already handling an exception! give up
			logger.error("unexpected exception when handling another exception: " + e.getMessage(),
				e);
			return new ErrorCodeRequestHandler(500);
		}
	}
