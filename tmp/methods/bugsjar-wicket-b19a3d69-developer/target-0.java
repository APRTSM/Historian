	public void sendRedirect(String location) throws IOException
	{
		redirectLocation = location;
		status = HttpServletResponse.SC_FOUND;
	}
