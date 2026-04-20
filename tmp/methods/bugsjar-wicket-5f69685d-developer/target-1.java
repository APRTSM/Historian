	private String relative(final String url)
	{
		Args.notEmpty(url, "location");

		if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/"))
		{
			return url;
		}

		RequestCycle rc = RequestCycle.get();
		return rc.getUrlRenderer().renderContextRelativeUrl(url);
	}
