	public ServletInputStream getInputStream() throws IOException
	{
		byte[] request = buildRequest();

		// Ok lets make an input stream to return
		final ByteArrayInputStream bais = new ByteArrayInputStream(request);

		return new ServletInputStream()
		{
			@Override
			public int read()
			{
				return bais.read();
			}
		};
	}
