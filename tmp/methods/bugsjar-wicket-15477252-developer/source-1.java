		protected final void writeStream(Attributes attributes, InputStream stream)
		{
			final Response response = attributes.getResponse();
			OutputStream s = new OutputStream()
			{
				@Override
				public void write(int b) throws IOException
				{
					response.write(new byte[] { (byte)b });
				}

				@Override
				public void write(byte[] b) throws IOException
				{
					response.write(b);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException
				{
					if (off == 0 || len == b.length)
					{
						write(b);
					}
					else
					{
						byte copy[] = new byte[len];
						System.arraycopy(b, off, copy, 0, len);
						write(copy);
					}
				}
			};
			try
			{
				Streams.copy(stream, s);
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
	public Time lastModifiedTime()
	{
		if (file != null)
		{
			// in case the file has been removed by now
			if (file.exists() == false)
			{
				return null;
			}

			long lastModified = file.lastModified();

			// if last modified changed update content length and last modified date
			if (lastModified != this.lastModified)
			{
				this.lastModified = lastModified;
				contentLength = (int)file.length();
			}
		}
		else
		{
			try
			{
				long lastModified = Connections.getLastModified(url);

				// if last modified changed update content length and last modified date
				if (lastModified != this.lastModified)
				{
					this.lastModified = lastModified;

					URLConnection connection = url.openConnection();
					contentLength = connection.getContentLength();
					Connections.close(connection);
				}
			}
			catch (IOException e)
			{
				if (url.toString().indexOf(".jar!") >= 0)
				{
					if (log.isDebugEnabled())
					{
						log.debug("getLastModified for " + url + " failed: " + e.getMessage());
					}
				}
				else
				{
					log.warn("getLastModified for " + url + " failed: " + e.getMessage());
				}

				// allow modification watcher to detect the problem
				return null;
			}

		}
		return Time.milliseconds(lastModified);
	}
