	private <X> Output<X> createChainedCollector(StreamConfig chainedTaskConfig) {


		// We create a wrapper that will encapsulate the chained operators and
		// network outputs

		OutputSelectorWrapper<?> outputSelectorWrapper = chainedTaskConfig.getOutputSelectorWrapper(cl);
		CollectorWrapper wrapper = new CollectorWrapper(outputSelectorWrapper);

		// Create collectors for the network outputs
		for (StreamEdge outputEdge : chainedTaskConfig.getNonChainedOutputs(cl)) {
			Collector<?> outCollector = outputMap.get(outputEdge);

			wrapper.addCollector(outCollector, outputEdge);
		}

		// Create collectors for the chained outputs
		for (StreamEdge outputEdge : chainedTaskConfig.getChainedOutputs(cl)) {
			Integer output = outputEdge.getTargetID();

			Collector<?> outCollector = createChainedCollector(chainedConfigs.get(output));

			wrapper.addCollector(outCollector, outputEdge);
		}

		if (chainedTaskConfig.isChainStart()) {
			// The current task is the first chained task at this vertex so we
			// return the wrapper
			return (Output<X>) wrapper;
		} else {
			// The current task is a part of the chain so we get the chainable
			// operator which will be returned and set it up using the wrapper
			OneInputStreamOperator chainableOperator =
					chainedTaskConfig.getStreamOperator(vertex.getUserCodeClassLoader());
			chainableOperator.setup(wrapper, vertex.context);

			chainedOperators.add(chainableOperator);
			return new OperatorCollector<X>(chainableOperator);
		}

	}
	public void registerInputOutput() {
		this.userClassLoader = getUserCodeClassLoader();
		this.configuration = new StreamConfig(getTaskConfiguration());
		this.context = createRuntimeContext(getEnvironment().getTaskName());
		this.stateHandleProvider = getStateHandleProvider();

		outputHandler = new OutputHandler<OUT>(this);

		streamOperator = configuration.getStreamOperator(userClassLoader);
		if (streamOperator != null) {
			// IterationHead and IterationTail don't have an Operator...
			streamOperator.setup(outputHandler.getOutput(), this.context);
		}

		hasChainedOperators = !outputHandler.getChainedOperators().isEmpty();
	}
	public StreamingRuntimeContext createRuntimeContext(String taskName) {
		Environment env = getEnvironment();
		return new StreamingRuntimeContext(taskName, env, getUserCodeClassLoader(),
				getExecutionConfig());
	}
