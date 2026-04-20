	public void addCookie(final Cookie cookie)
	{
		// remove any potential duplicates
		// see http://www.ietf.org/rfc/rfc2109.txt, p.4.3.3
		Iterator<Cookie> iterator = cookies.iterator();
		while (iterator.hasNext())
		{
			Cookie old = iterator.next();
			if (cookie.getName().equals(old.getName()) &&
				((cookie.getPath() == null && old.getPath() == null) || (cookie.getPath().equals(old.getPath()))) &&
				((cookie.getDomain() == null && old.getDomain() == null) || (cookie.getDomain().equals(old.getDomain()))))
			{
				iterator.remove();
			}
		}
		cookies.add(cookie);
	}
