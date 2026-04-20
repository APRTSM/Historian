	public static StreamExecutionEnvironment createRemoteEnvironment(String host, int port,
			String... jarFiles) {
		return new RemoteStreamEnvironment(host, port, jarFiles);
	}
	public StreamGraph getStreamGraph() {
		if (transformations.size() <= 0) {
			throw new IllegalStateException("No operators defined in streaming topology. Cannot execute.");
		}
		return StreamGraphGenerator.generate(this, transformations);
	}
		public Tuple getKey(IN value) throws Exception {
			Tuple key = Tuple.getTupleClass(fields.length).newInstance();
			for (int i = 0; i < fields.length; i++) {
				key.setField(Array.get(value, fields[i]), i);
			}
			return key;
		}
		public Tuple getKey(IN value) throws Exception {
			key = Tuple.getTupleClass(keyLength).newInstance();
			comparator.extractKeys(value, keyArray, 0);
			for (int i = 0; i < keyLength; i++) {
				key.setField(keyArray[i], i);
			}
			return key;
		}
