	public AppendingStringBuffer insert(final int offset, final Object obj)
	{
		if (obj instanceof AppendingStringBuffer)
		{
			AppendingStringBuffer asb = (AppendingStringBuffer)obj;
			return insert(offset, asb.value, 0, asb.count);
		}
		else if (obj instanceof StringBuffer)
		{
			return insert(offset, obj);
		}
		return insert(offset, String.valueOf(obj));
	}
