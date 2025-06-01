	public static File getLocalFileFromUrl(URL url)
	{
		return getLocalFileFromUrl(Args.notNull(url, "url").toExternalForm());
	}
