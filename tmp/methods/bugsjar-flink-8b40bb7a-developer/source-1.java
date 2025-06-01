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
