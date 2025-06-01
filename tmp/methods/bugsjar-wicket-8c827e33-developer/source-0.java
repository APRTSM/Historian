	private void checkHeader()
	{
		if (bufferedWritten)
		{
			throw new IllegalStateException("Header was already written to response!");
		}
	}
	public void sendRedirect(String url)
	{
		checkHeader();
		bufferedResponse.sendRedirect(url);
	}
	public void write(CharSequence sequence)
	{
		writeBuffered();
		originalResponse.write(sequence);
	}
	public void write(byte[] array)
	{
		writeBuffered();
		originalResponse.write(array);
	}
	public void addHeader(String name, String value)
	{
		checkHeader();
		bufferedResponse.addHeader(name, value);
	}
	public void setContentType(String mimeType)
	{
		checkHeader();
		bufferedResponse.setContentType(mimeType);
	}
	public void setDateHeader(String name, Time date)
	{
		Args.notNull(date, "date");
		checkHeader();
		bufferedResponse.setDateHeader(name, date);
	}
	public void setHeader(String name, String value)
	{
		checkHeader();
		bufferedResponse.setHeader(name, value);
	}
	public void reset()
	{
		if (flushed)
		{
			throw new IllegalStateException("Response has already been flushed!");
		}
		bufferedResponse.reset();
		bufferedWritten = false;
	}
	public void setStatus(int sc)
	{
		bufferedResponse.setStatus(sc);
	}
	public void sendError(int sc, String msg)
	{
		checkHeader();
		bufferedResponse.sendError(sc, msg);
	}
	public void flush()
	{
		if (!bufferedWritten)
		{
			bufferedResponse.writeTo(originalResponse);
			bufferedResponse.reset();
		}
		originalResponse.flush();
		flushed = true;
	}
	private void writeBuffered()
	{
		if (!bufferedWritten)
		{
			bufferedResponse.writeTo(originalResponse);
			bufferedWritten = true;
		}
	}
	public void clearCookie(Cookie cookie)
	{
		checkHeader();
		bufferedResponse.clearCookie(cookie);
	}
	public boolean isRedirect()
	{
		return bufferedResponse.isRedirect();
	}
	public void write(byte[] array, int offset, int length)
	{
		writeBuffered();
		originalResponse.write(array, offset, length);
	}
	public void addCookie(Cookie cookie)
	{
		checkHeader();
		bufferedResponse.addCookie(cookie);
	}
	public void setContentLength(long length)
	{
		checkHeader();
		bufferedResponse.setContentLength(length);
	}
