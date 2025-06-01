	public AppendingStringBuffer insert(final int offset, final Object obj)
	{
		if (obj instanceof AppendingStringBuffer)
		{
			AppendingStringBuffer asb = (AppendingStringBuffer)obj;
			return insert(offset, asb.value, 0, asb.count);
		}
		else if (obj instanceof StringBuffer)
		{
			return insert(offset, (StringBuffer)obj);
		}
		else if (obj instanceof StringBuilder)
		{
			return insert(offset, (StringBuilder)obj);
		}
		return insert(offset, String.valueOf(obj));
	}
	public AppendingStringBuffer insert(final int offset, StringBuffer str)
	{
		if ((offset < 0) || (offset > count))
		{
			throw new StringIndexOutOfBoundsException();
		}

		if (str == null)
		{
			str = SBF_NULL;
		}
		int len = str.length();
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		System.arraycopy(value, offset, value, offset + len, count - offset);
		str.getChars(0, len, value, offset);
		count = newcount;
		return this;
	}
