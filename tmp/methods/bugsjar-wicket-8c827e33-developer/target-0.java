	public void reset()
	{
		if (buffering)
		{
			// still buffering so just reset the buffer of meta data
			bufferedResponse.reset();
		}
		else
		{
			// the original response is never reset (see class javadoc)
			throw new IllegalStateException("Response is no longer buffering!");
		}
	}
	public void setHeader(String name, String value)
	{
		getMetaResponse().setHeader(name, value);
	}
	public HeaderBufferingWebResponse(WebResponse originalResponse)
	{
		this.originalResponse = originalResponse;

		bufferedResponse = new BufferedWebResponse(originalResponse);
	}
	public void setContentType(String mimeType)
	{
		getMetaResponse().setContentType(mimeType);
	}
	public void setDateHeader(String name, Time date)
	{
		Args.notNull(date, "date");
		getMetaResponse().setDateHeader(name, date);
	}
	public void addHeader(String name, String value)
	{
		getMetaResponse().addHeader(name, value);
	}
	private void stopBuffering()
	{
		if (buffering)
		{
			bufferedResponse.writeTo(originalResponse);
			buffering = false;
		}
	}
	public void sendError(int sc, String msg)
	{
		getMetaResponse().sendError(sc, msg);
	}
	private WebResponse getMetaResponse()
	{
		if (buffering)
		{
			return bufferedResponse;
		}
		else
		{
			return originalResponse;
		}
	}
	public void sendRedirect(String url)
	{
		getMetaResponse().sendRedirect(url);
	}
	public void write(byte[] array, int offset, int length)
	{
		stopBuffering();

		originalResponse.write(array, offset, length);
	}
	public void setStatus(int sc)
	{
		getMetaResponse().setStatus(sc);
	}
	public void flush()
	{
		stopBuffering();

		originalResponse.flush();
	}
	public void addCookie(Cookie cookie)
	{
		getMetaResponse().addCookie(cookie);
	}
	public void write(byte[] array)
	{
		stopBuffering();

		originalResponse.write(array);
	}
	public void write(CharSequence sequence)
	{
		stopBuffering();

		originalResponse.write(sequence);
	}
	public boolean isRedirect()
	{
		return getMetaResponse().isRedirect();
	}
	public void clearCookie(Cookie cookie)
	{
		getMetaResponse().clearCookie(cookie);
	}
	public void setContentLength(long length)
	{
		getMetaResponse().setContentLength(length);
	}
