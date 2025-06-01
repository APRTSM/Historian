	public static Time getLastModified(final URL url) throws IOException
	{
		// check if url points to a local file
		final File file = Files.getLocalFileFromUrl(url);

		if (file != null)
		{
			// in that case we can get the timestamp faster
			return Files.getLastModified(file);
		}

		// otherwise open the url and proceed
		URLConnection connection = url.openConnection();

		final long milliseconds;

		try
		{
			if (connection instanceof JarURLConnection)
			{
				JarURLConnection jarUrlConnection = (JarURLConnection)connection;
				URL jarFileUrl = jarUrlConnection.getJarFileURL();
				URLConnection jarFileConnection = jarFileUrl.openConnection();
				jarFileConnection.setDoInput(false);
				// get timestamp from JAR
				milliseconds = jarFileConnection.getLastModified();
			}
			else
			{
				// get timestamp from URL
				milliseconds = connection.getLastModified();
			}

			// return null if timestamp is unavailable
			if (milliseconds == 0)
			{
				return null;
			}

			// return UNIX timestamp
			return Time.millis(milliseconds);

		}
		finally
		{
			closeQuietly(connection);
		}
	}
