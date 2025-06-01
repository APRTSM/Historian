	public Set keySet()
	{
		return new AbstractSet()
		{
			public Iterator iterator()
			{
				return new Iterator()
				{
					public boolean hasNext()
					{
						return i < size;
					}

					public Object next()
					{
						// Find next key
						i = nextKey(nextIndex(i));

						// Just in case... (WICKET-428)
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						
						// Get key
						return keys[i];
					}

					public void remove()
					{
						keys[i] = null;
						values[i] = null;
						size--;
					}

					int i = -1;
				};
			}

			public int size()
			{
				return size;
			}
		};
	}
	public Collection values()
	{
		return new AbstractList()
		{
			public Object get(final int index)
			{
				int keyIndex = nextKey(0);

				for (int i = 0; i < index; i++)
				{
					keyIndex = nextKey(keyIndex + 1);
				}

				return values[keyIndex];
			}

			public int size()
			{
				return size;
			}
		};
	}
