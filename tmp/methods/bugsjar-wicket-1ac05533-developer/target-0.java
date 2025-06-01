	public PageParameters mergeWith(final PageParameters other)
	{
		if (this != other)
		{
			for (int index = 0; index < other.getIndexedCount(); index++)
			{
				if (!other.get(index).isNull())
				{
					set(index, other.get(index));
				}
			}
			for (String name : other.getNamedKeys())
			{
				remove(name);
			}
			for (NamedPair curNamed : other.getAllNamed())
			{
				add(curNamed.getKey(), curNamed.getValue());
			}
		}
		return this;
	}
