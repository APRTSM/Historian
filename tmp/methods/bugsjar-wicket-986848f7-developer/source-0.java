	public ServletInputStream getInputStream() throws IOException
	{
		if (uploadedFiles != null && uploadedFiles.size() > 0)
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
		else
		{
			return new ServletInputStream()
			{
				@Override
				public int read()
				{
					return -1;
				}
			};
		}
	}
