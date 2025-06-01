	private void readObject(final java.io.ObjectInputStream s) throws IOException,
		ClassNotFoundException
	{
		modCount = new AtomicInteger(0);

		// Read in the threshold, loadfactor, and any hidden stuff
		s.defaultReadObject();

		// Read in number of buckets and allocate the bucket array;
		int numBuckets = s.readInt();
		table = new Entry[numBuckets];

		init(); // Give subclass a chance to do its thing.

		// Read in size (number of Mappings)
		int size = s.readInt();

		// Read the keys and values, and put the mappings in the HashMap
		for (int i = 0; i < size; i++)
		{
			int key = s.readInt();
			V value = (V)s.readObject();
			putForCreate(key, value);
		}
	}
