	public final Component get(int index)
	{
		Component childAtIndex = null;
		Iterator<Component> childIterator = iterator();
		while (index-- >= 0 && childIterator.hasNext())
		{
			childAtIndex = childIterator.next();
		}
		if(childAtIndex == null) 
		{
			throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
		}
		return childAtIndex;
	}
