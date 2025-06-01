	public void setInputsOutputs() {
		try {
			inputHandler = new InputHandler<IN>(this);

			iterationId = configuration.getIterationId();
			iterationWaitTime = configuration.getIterationWaitTime();
			shouldWait = iterationWaitTime > 0;
			BlockingQueueBroker.instance().get(iterationId.toString()+"-"
					+getEnvironment().getIndexInSubtaskGroup());
		} catch (Exception e) {
			throw new StreamVertexException(String.format(
					"Cannot register inputs of StreamIterationSink %s", iterationId), e);
		}
	}
