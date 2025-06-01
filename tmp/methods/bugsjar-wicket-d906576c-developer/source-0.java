	public Set entrySet()
	{
		return new AbstractSet()
		{
			public Iterator iterator()
			{
				return new Iterator()
				{
					public boolean hasNext()
					{
						return index < MicroMap.this.size();
					}

					public Object next()
					{
						index++;

						return new Map.Entry()
						{
							public Object getKey()
							{
								return key;
							}

							public Object getValue()
							{
								return value;
							}

							public Object setValue(final Object value)
							{
								final Object oldValue = MicroMap.this.value;

								MicroMap.this.value = value;

								return oldValue;
							}
						};
					}

					public void remove()
					{
						clear();
					}

					int index = 0;
				};
			}

			public int size()
			{
				return MicroMap.this.size();
			}
		};
	}
	public Collection values()
	{
		return new AbstractList()
		{
			public Object get(final int index)
			{
				return value;
			}

			public int size()
			{
				return MicroMap.this.size();
			}
		};
	}
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
						return index < MicroMap.this.size();
					}

					public Object next()
					{
						index++;

						return key;
					}

					public void remove()
					{
						MicroMap.this.clear();
					}

					int index;
				};
			}

			public int size()
			{
				return MicroMap.this.size();
			}
		};
	}
