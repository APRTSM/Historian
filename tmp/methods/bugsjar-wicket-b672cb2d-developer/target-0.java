	public static File getLocalFileFromUrl(URL url)
	{
		final URL location = Args.notNull(url, "url");

		try
		{
			return getLocalFileFromUrl(URLDecoder.decode(location.toExternalForm(), "UTF-8"));
		}
		catch (UnsupportedEncodingException ex)
		{
			return null;
		}
	}
