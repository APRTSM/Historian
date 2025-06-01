	public static StreamExecutionEnvironment createRemoteEnvironment(String host, int port,
			String... jarFiles) {
		RemoteStreamEnvironment env = new RemoteStreamEnvironment(host, port, jarFiles);
		return env;
	}
	public StreamGraph getStreamGraph() {
		if (transformations.size() <= 0) {
			throw new IllegalStateException("No operators defined in streaming topology. Cannot execute.");
		}
		StreamGraph result = StreamGraphGenerator.generate(this, transformations);
		return result;
	}
		public Tuple getKey(IN value) throws Exception {
			key = (Tuple) tupleClasses[keyLength - 1].newInstance();
			comparator.extractKeys(value, keyArray, 0);
			for (int i = 0; i < keyLength; i++) {
				key.setField(keyArray[i], i);
			}
			return key;
		}
		public Tuple getKey(IN value) throws Exception {
			key = (Tuple) tupleClasses[fields.length - 1].newInstance();
			for (int i = 0; i < fields.length; i++) {
				int pos = fields[i];
				key.setField(Array.get(value, fields[pos]), i);
			}
			return key;
		}
