	public void addCookie(final Cookie cookie)
	{
		// remove any potential duplicates
		cookies.remove(cookie);
		cookies.add(cookie);
	}
