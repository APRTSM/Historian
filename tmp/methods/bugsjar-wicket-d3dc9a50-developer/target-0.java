	public static boolean isRelative(final String url)
	{
		// the regex means "doesn't start with 'scheme://'"
		if ((url != null) && (url.startsWith("/") == false) && (!url.matches("^\\w+\\:\\/\\/.*")) &&
			!(url.startsWith("#")))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
