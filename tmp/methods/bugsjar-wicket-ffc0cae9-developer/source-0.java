	public boolean processRequest()
	{
		try
		{
			set(this);
			IRequestHandler handler = resolveRequestHandler();
			if (handler != null)
			{
				executeRequestHandler(handler);
				return true;
			}

			// Did not find any suitable handler, thus not executing the request
			log.debug(
				"No suitable handler found for URL {}, falling back to container to process this request",
				request.getUrl());
		}
		catch (Exception e)
		{
			IRequestHandler handler = handleException(e);
			if (handler != null)
			{
				executeExceptionRequestHandler(handler, getExceptionRetryCount());
			}
			else
			{
				log.error("Error during request processing. URL=" + request.getUrl(), e);
			}
			return true;
		}
		finally
		{
			set(null);
		}
		return false;
	}
	public boolean processRequestAndDetach()
	{
		boolean result;
		try
		{
			listeners.onBeginRequest(this);
			onBeginRequest();
			result = processRequest();
		}
		finally
		{
			detach();
		}
		return result;
	}
