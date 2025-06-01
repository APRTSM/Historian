	public final Component get(int index)
	{
		final int requestedIndex = index;
		Component childAtIndex = null;
		Iterator<Component> childIterator = iterator();
		while (index >= 0 && childIterator.hasNext())
		{
			childAtIndex = childIterator.next();
			index--;
		}
		if(index >= 0 || childAtIndex == null)
		{
			throw new ArrayIndexOutOfBoundsException(Integer.toString(requestedIndex));
		}
		return childAtIndex;
	}
