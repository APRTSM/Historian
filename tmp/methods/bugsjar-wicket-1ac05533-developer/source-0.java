	public PageParameters mergeWith(final PageParameters other)
	{
		if (this != other)
		{
			for (int index = 0; index < other.getIndexedCount(); index++)
				set(index, other.get(index));
			for (NamedPair curNamed : other.getAllNamed())
				set(curNamed.getKey(), curNamed.getValue());
		}
		return this;
	}
