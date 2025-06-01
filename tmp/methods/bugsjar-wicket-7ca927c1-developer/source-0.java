	public HttpSession getSession()
	{
		if (session instanceof MockHttpSession && ((MockHttpSession)session).isTemporary())
		{
			return null;
		}
		return session;
	}
	public HttpSession getSession(boolean b)
	{
		if (b && session instanceof MockHttpSession)
		{
			((MockHttpSession)session).setTemporary(false);
		}
		return getSession();
	}
