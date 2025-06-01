	public static boolean isRelative(String url)
	{
		if ((url != null) && (url.startsWith("/") == false) && (url.indexOf("://") < 0) &&
			!(url.startsWith("#")))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
