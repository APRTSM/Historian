	private String relative(final String location)
	{
		Args.notEmpty(location, "location");

		if (location.startsWith("http://") || location.startsWith("https://") ||
			location.startsWith("/"))
		{
			return location;
		}

		RequestCycle rc = RequestCycle.get();
		return rc.getUrlRenderer().renderUrl(Url.parse(location, rc.getRequest().getCharset()));
	}
