	protected void run() throws Exception {
		
		final String iterationId = configuration.getIterationId();
		if (iterationId == null || iterationId.length() == 0) {
			throw new Exception("Missing iteration ID in the task configuration");
		}
		
		final String brokerID = createBrokerIdString(getEnvironment().getJobID(), iterationId ,
				getEnvironment().getIndexInSubtaskGroup());
		
		final long iterationWaitTime = configuration.getIterationWaitTime();
		final boolean shouldWait = iterationWaitTime > 0;

		final BlockingQueue<StreamRecord<OUT>> dataChannel = new ArrayBlockingQueue<StreamRecord<OUT>>(1);

		// offer the queue for the tail
		BlockingQueueBroker.INSTANCE.handIn(brokerID, dataChannel);
		LOG.info("Iteration head {} added feedback queue under {}", getName(), brokerID);

		// do the work 
		try {
			@SuppressWarnings("unchecked")
			Collection<RecordWriterOutput<OUT>> outputs = 
					(Collection<RecordWriterOutput<OUT>>) (Collection<?>) outputHandler.getOutputs();

			// If timestamps are enabled we make sure to remove cyclic watermark dependencies
			if (getExecutionConfig().areTimestampsEnabled()) {
				for (RecordWriterOutput<OUT> output : outputs) {
					output.emitWatermark(new Watermark(Long.MAX_VALUE));
				}
			}

			while (running) {
				StreamRecord<OUT> nextRecord = shouldWait ?
					dataChannel.poll(iterationWaitTime, TimeUnit.MILLISECONDS) :
					dataChannel.take();

				if (nextRecord != null) {
					for (RecordWriterOutput<OUT> output : outputs) {
						output.collect(nextRecord);
					}
				}
				else {
					// done
					break;
				}
			}
		}
		finally {
			// make sure that we remove the queue from the broker, to prevent a resource leak
			BlockingQueueBroker.INSTANCE.remove(brokerID);
			LOG.info("Iteration head {} removed feedback queue under {}", getName(), brokerID);
		}
	}
