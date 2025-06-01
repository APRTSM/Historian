	public final void cancel() throws Exception {
		isRunning = false;
		canceled = true;
		cancelTask();
	}
	public final void invoke() throws Exception {

		boolean disposed = false;
		try {
			// -------- Initialize ---------
			LOG.debug("Initializing {}", getName());

			userClassLoader = getUserCodeClassLoader();
			configuration = new StreamConfig(getTaskConfiguration());
			accumulatorMap = getEnvironment().getAccumulatorRegistry().getUserMap();

			headOperator = configuration.getStreamOperator(userClassLoader);
			operatorChain = new OperatorChain<>(this, headOperator, 
						getEnvironment().getAccumulatorRegistry().getReadWriteReporter());

			if (headOperator != null) {
				headOperator.setup(this, configuration, operatorChain.getChainEntryPoint());
			}

			timerService = Executors.newSingleThreadScheduledExecutor(
					new DispatcherThreadFactory(TRIGGER_THREAD_GROUP, "Time Trigger for " + getName()));

			// task specific initialization
			init();
			
			// save the work of reloadig state, etc, if the task is already canceled
			if (canceled) {
				throw new CancelTaskException();
			}

			// -------- Invoke --------
			LOG.debug("Invoking {}", getName());
			
			// first order of business is to give operators back their state
			stateBackend = createStateBackend();
			stateBackend.initializeForJob(getEnvironment());
			restoreState();
			
			// we need to make sure that any triggers scheduled in open() cannot be
			// executed before all operators are opened
			synchronized (lock) {
				openAllOperators();
			}

			// final check to exit early before starting to run
			if (canceled) {
				throw new CancelTaskException();
			}

				// let the task do its work
			isRunning = true;
			run();
			isRunning = false;
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Finished task {}", getName());
			}
			
			// make sure no further checkpoint and notification actions happen.
			// we make sure that no other thread is currently in the locked scope before
			// we close the operators by trying to acquire the checkpoint scope lock
			// we also need to make sure that no triggers fire concurrently with the close logic
			synchronized (lock) {
				// this is part of the main logic, so if this fails, the task is considered failed
				closeAllOperators();
			}
			
			// make sure all buffered data is flushed
			operatorChain.flushOutputs();

			// make an attempt to dispose the operators such that failures in the dispose call
			// still let the computation fail
			tryDisposeAllOperators();
			disposed = true;
		}
		finally {
			// clean up everything we initialized
			isRunning = false;

			// stop all timers and threads
			if (timerService != null) {
				try {
					timerService.shutdownNow();
				}
				catch (Throwable t) {
					// catch and log the exception to not replace the original exception
					LOG.error("Could not shut down timer service", t);
				}
			}
			
			// stop all asynchronous checkpoint threads
			try {
				for (Thread checkpointThread : asyncCheckpointThreads) {
					checkpointThread.interrupt();
				}
				asyncCheckpointThreads.clear();
			}
			catch (Throwable t) {
				// catch and log the exception to not replace the original exception
				LOG.error("Could not shut down async checkpoint threads", t);
			}
			
			// release the output resources. this method should never fail.
			if (operatorChain != null) {
				operatorChain.releaseOutputs();
			}

			// we must! perform this cleanup
			try {
				cleanup();
			}
			catch (Throwable t) {
				// catch and log the exception to not replace the original exception
				LOG.error("Error during cleanup of stream task", t);
			}
			
			// if the operators were not disposed before, do a hard dispose
			if (!disposed) {
				disposeAllOperators();
			}

			try {
				if (stateBackend != null) {
					stateBackend.close();
				}
			} catch (Throwable t) {
				LOG.error("Error while closing the state backend", t);
			}
		}
	}
	public final boolean isCanceled() {
		return canceled;
	}
