	public HttpSession getSession(boolean b)
	{
		HttpSession sess = null;
		if (session instanceof MockHttpSession)
		{
			MockHttpSession mockHttpSession = (MockHttpSession) session;
			if (b)
			{
				mockHttpSession.setTemporary(false);
			}

			if (mockHttpSession.isTemporary() == false)
			{
				sess = session;
			}
		}
		return sess;
	}
	public HttpSession getSession()
	{
		return getSession(true);
	}
