	public void addHeader(String name, String value)
	{
		// be lenient and strip leading / trailing blanks
		value = Args.notEmpty(value, "value").trim();

		internalAdd(name, value);
	}
