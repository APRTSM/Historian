	final Url mapHandler(IRequestHandler handler, Request request)
	{
		Url url = delegate.mapHandler(handler);

		Scheme desired = getDesiredSchemeFor(handler);
		Scheme current = getSchemeOf(request);
		if (!desired.isCompatibleWith(current))
		{
			// the generated url does not have the correct scheme, set it (which in turn will cause
			// the url to be rendered in its full representation)
			url.setProtocol(desired.urlName());
			if (url.getPort() != null || !desired.usesStandardPort(config))
			{
				url.setPort(desired.getPort(config));
			}
		}
		return url;
	}
