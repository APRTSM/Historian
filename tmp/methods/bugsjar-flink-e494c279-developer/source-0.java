	public static <X, K> KeySelector<X, K> getSelectorForOneKey(Keys<X> keys, Partitioner<K> partitioner, TypeInformation<X> typeInfo,
			ExecutionConfig executionConfig) {
		if (partitioner != null) {
			keys.validateCustomPartitioner(partitioner, null);
		}

		int[] logicalKeyPositions = keys.computeLogicalKeyPositions();

		if (logicalKeyPositions.length != 1) {
			throw new IllegalArgumentException("There must be exactly 1 key specified");
		}

		TypeComparator<X> comparator = ((CompositeType<X>) typeInfo).createComparator(
				logicalKeyPositions, new boolean[1], 0, executionConfig);
		return new OneKeySelector<X, K>(comparator);
	}
	public static <X> KeySelector<X, Tuple> getSelectorForKeys(Keys<X> keys, TypeInformation<X> typeInfo, ExecutionConfig executionConfig) {
		if (!(typeInfo instanceof CompositeType)) {
			throw new InvalidTypesException(
					"This key operation requires a composite type such as Tuples, POJOs, or Case Classes.");
		}

		CompositeType<X> compositeType = (CompositeType<X>) typeInfo;
		
		int[] logicalKeyPositions = keys.computeLogicalKeyPositions();
		int numKeyFields = logicalKeyPositions.length;
		
		// use ascending order here, the code paths for that are usually a slight bit faster
		boolean[] orders = new boolean[numKeyFields];
		for (int i = 0; i < numKeyFields; i++) {
			orders[i] = true;
		}
		
		TypeComparator<X> comparator = compositeType.createComparator(logicalKeyPositions, orders, 0, executionConfig);
		return new ComparableKeySelector<X>(comparator, numKeyFields);
	}
		public ComparableKeySelector(TypeComparator<IN> comparator, int keyLength) {
			this.comparator = comparator;
			this.keyLength = keyLength;
			keyArray = new Object[keyLength];
		}
