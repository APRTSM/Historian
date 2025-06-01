	public final String getEncoding()
	{
		return xmlReader.getEncoding();
	}
	public void parse(final CharSequence string) throws IOException
	{
		parse(new ByteArrayInputStream(string.toString().getBytes()), null);
	}
	public void parse(final InputStream inputStream, final String encoding) throws IOException
	{
		Args.notNull(inputStream, "inputStream");

		try
		{
			xmlReader = new XmlReader(new BufferedInputStream(inputStream, 4000), encoding);
			input = new FullyBufferedReader(xmlReader);
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(xmlReader);
		}
	}
