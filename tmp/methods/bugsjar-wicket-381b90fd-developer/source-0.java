	public static boolean isEqual(Cookie c1, Cookie c2)
	{
		Args.notNull(c1, "c1");
		Args.notNull(c2, "c2");

		return c1.getName().equals(c2.getName()) &&
				((c1.getPath() == null && c2.getPath() == null) || (c1.getPath().equals(c2.getPath()))) &&
				((c1.getDomain() == null && c2.getDomain() == null) || (c1.getDomain().equals(c2.getDomain())));
	}
