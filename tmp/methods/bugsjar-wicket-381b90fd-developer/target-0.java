	public static boolean isEqual(Cookie c1, Cookie c2)
	{
		Args.notNull(c1, "c1");
		Args.notNull(c2, "c2");

		return c1.getName().equals(c2.getName()) &&
				Objects.isEqual(c1.getPath(), c2.getPath()) &&
				Objects.isEqual(c1.getDomain(), c2.getDomain());
	}
