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
						return i < size - 1;
					}

					public Object next()
					{
						// Just in case... (WICKET-428)
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						
						// Find next key
						i = nextKey(nextIndex(i));

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
				if (index > size - 1) {
					throw new IndexOutOfBoundsException();
				}
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
