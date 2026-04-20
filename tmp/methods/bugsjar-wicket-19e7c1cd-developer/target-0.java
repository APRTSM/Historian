	public void parse(final InputStream inputStream, final String encoding) throws IOException
	{
		Args.notNull(inputStream, "inputStream");

		try
		{
			XmlReader xmlReader = new XmlReader(new BufferedInputStream(inputStream, 4000),
				encoding);
			this.input = new FullyBufferedReader(xmlReader);
			this.encoding = xmlReader.getEncoding();
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}
	public void parse(final CharSequence string) throws IOException
	{
		Args.notNull(string, "string");

		this.input = new FullyBufferedReader(new StringReader(string.toString()));
		this.encoding = null;
	}
	public final String getEncoding()
	{
		return encoding;
	}
