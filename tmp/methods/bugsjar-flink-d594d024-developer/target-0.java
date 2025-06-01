	private void cancelInvokable() {
		// in case of an exception during execution, we still call "cancel()" on the task
		if (invokable != null && this.invokable != null && invokableHasBeenCanceled.compareAndSet(false, true)) {
			try {
				invokable.cancel();
			}
			catch (Throwable t) {
				LOG.error("Error while canceling task " + taskNameWithSubtask, t);
			}
		}
	}
	public void run() {

		// ----------------------------
		//  Initial State transition
		// ----------------------------
		while (true) {
			ExecutionState current = this.executionState;
			if (current == ExecutionState.CREATED) {
				if (STATE_UPDATER.compareAndSet(this, ExecutionState.CREATED, ExecutionState.DEPLOYING)) {
					// success, we can start our work
					break;
				}
			}
			else if (current == ExecutionState.FAILED) {
				// we were immediately failed. tell the TaskManager that we reached our final state
				notifyFinalState();
				return;
			}
			else if (current == ExecutionState.CANCELING) {
				if (STATE_UPDATER.compareAndSet(this, ExecutionState.CANCELING, ExecutionState.CANCELED)) {
					// we were immediately canceled. tell the TaskManager that we reached our final state
					notifyFinalState();
					return;
				}
			}
			else {
				throw new IllegalStateException("Invalid state for beginning of task operation");
			}
		}

		// all resource acquisitions and registrations from here on
		// need to be undone in the end
		Map<String, Future<Path>> distributedCacheEntries = new HashMap<String, Future<Path>>();
		AbstractInvokable invokable = null;

		try {
			// ----------------------------
			//  Task Bootstrap - We periodically 
			//  check for canceling as a shortcut
			// ----------------------------

			// first of all, get a user-code classloader
			// this may involve downloading the job's JAR files and/or classes
			LOG.info("Loading JAR files for task " + taskNameWithSubtask);
			final ClassLoader userCodeClassLoader = createUserCodeClassloader(libraryCache);

			// now load the task's invokable code
			invokable = loadAndInstantiateInvokable(userCodeClassLoader, nameOfInvokableClass);

			if (isCanceledOrFailed()) {
				throw new CancelTaskException();
			}

			// ----------------------------------------------------------------
			// register the task with the network stack
			// this operation may fail if the system does not have enough
			// memory to run the necessary data exchanges
			// the registration must also strictly be undone
			// ----------------------------------------------------------------

			LOG.info("Registering task at network: " + this);
			network.registerTask(this);

			// next, kick off the background copying of files for the distributed cache
			try {
				for (Map.Entry<String, DistributedCache.DistributedCacheEntry> entry :
						DistributedCache.readFileInfoFromConfig(jobConfiguration))
				{
					LOG.info("Obtaining local cache file for '" + entry.getKey() + '\'');
					Future<Path> cp = fileCache.createTmpFile(entry.getKey(), entry.getValue(), jobId);
					distributedCacheEntries.put(entry.getKey(), cp);
				}
			}
			catch (Exception e) {
				throw new Exception("Exception while adding files to distributed cache.", e);
			}

			if (isCanceledOrFailed()) {
				throw new CancelTaskException();
			}

			// ----------------------------------------------------------------
			//  call the user code initialization methods
			// ----------------------------------------------------------------

			TaskInputSplitProvider splitProvider = new TaskInputSplitProvider(jobManager,
					jobId, vertexId, executionId, userCodeClassLoader, actorAskTimeout);

			Environment env = new RuntimeEnvironment(jobId, vertexId, executionId,
					taskName, taskNameWithSubtask, subtaskIndex, parallelism,
					jobConfiguration, taskConfiguration,
					userCodeClassLoader, memoryManager, ioManager, broadcastVariableManager,
					splitProvider, distributedCacheEntries,
					writers, inputGates, jobManager);

			// let the task code create its readers and writers
			invokable.setEnvironment(env);
			try {
				invokable.registerInputOutput();
			}
			catch (Exception e) {
				throw new Exception("Call to registerInputOutput() of invokable failed", e);
			}

			// the very last thing before the actual execution starts running is to inject
			// the state into the task. the state is non-empty if this is an execution
			// of a task that failed but had backuped state from a checkpoint

			// get our private reference onto the stack (be safe against concurrent changes) 
			SerializedValue<StateHandle<?>> operatorState = this.operatorState;
			
			if (operatorState != null) {
				if (invokable instanceof OperatorStateCarrier) {
					try {
						StateHandle<?> state = operatorState.deserializeValue(userCodeClassLoader);
						OperatorStateCarrier<?> op = (OperatorStateCarrier<?>) invokable;
						StateUtils.setOperatorState(op, state);
					}
					catch (Exception e) {
						throw new RuntimeException("Failed to deserialize state handle and setup initial operator state.", e);
					}
				}
				else {
					throw new IllegalStateException("Found operator state for a non-stateful task invokable");
				}
			}

			// be memory and GC friendly - since the code stays in invoke() for a potentially long time,
			// we clear the reference to the state handle
			//noinspection UnusedAssignment
			operatorState = null;
			this.operatorState = null;

			// ----------------------------------------------------------------
			//  actual task core work
			// ----------------------------------------------------------------

			// we must make strictly sure that the invokable is accessible to the cancel() call
			// by the time we switched to running.
			this.invokable = invokable;

			// switch to the RUNNING state, if that fails, we have been canceled/failed in the meantime
			if (!STATE_UPDATER.compareAndSet(this, ExecutionState.DEPLOYING, ExecutionState.RUNNING)) {
				throw new CancelTaskException();
			}
			
			// notify everyone that we switched to running. especially the TaskManager needs
			// to know this!
			notifyObservers(ExecutionState.RUNNING, null);
			taskManager.tell(new TaskMessages.UpdateTaskExecutionState(
					new TaskExecutionState(jobId, executionId, ExecutionState.RUNNING)), ActorRef.noSender());

			// make sure the user code classloader is accessible thread-locally
			executingThread.setContextClassLoader(userCodeClassLoader);

			// run the invokable
			invokable.invoke();

			// make sure, we enter the catch block if the task leaves the invoke() method due
			// to the fact that it has been canceled
			if (isCanceledOrFailed()) {
				throw new CancelTaskException();
			}

			// ----------------------------------------------------------------
			//  finalization of a successful execution
			// ----------------------------------------------------------------

			// finish the produced partitions. if this fails, we consider the execution failed.
			for (ResultPartition partition : producedPartitions) {
				if (partition != null) {
					partition.finish();
				}
			}

			// try to mark the task as finished
			// if that fails, the task was canceled/failed in the meantime
			if (STATE_UPDATER.compareAndSet(this, ExecutionState.RUNNING, ExecutionState.FINISHED)) {
				notifyObservers(ExecutionState.FINISHED, null);
			}
			else {
				throw new CancelTaskException();
			}
		}
		catch (Throwable t) {

			// ----------------------------------------------------------------
			// the execution failed. either the invokable code properly failed, or
			// an exception was thrown as a side effect of cancelling
			// ----------------------------------------------------------------

			try {
				// transition into our final state. we should be either in DEPLOYING, RUNNING, CANCELING, or FAILED
				// loop for multiple retries during concurrent state changes via calls to cancel() or
				// to failExternally()
				while (true) {
					ExecutionState current = this.executionState;

					if (current == ExecutionState.RUNNING || current == ExecutionState.DEPLOYING) {
						if (t instanceof CancelTaskException) {
							if (STATE_UPDATER.compareAndSet(this, current, ExecutionState.CANCELED)) {
								cancelInvokable();

								notifyObservers(ExecutionState.CANCELED, null);
								break;
							}
						}
						else {
							if (STATE_UPDATER.compareAndSet(this, current, ExecutionState.FAILED)) {
								// proper failure of the task. record the exception as the root cause
								failureCause = t;
								cancelInvokable();

								notifyObservers(ExecutionState.FAILED, t);
								break;
							}
						}
					}
					else if (current == ExecutionState.CANCELING) {
						if (STATE_UPDATER.compareAndSet(this, current, ExecutionState.CANCELED)) {
							notifyObservers(ExecutionState.CANCELED, null);
							break;
						}
					}
					else if (current == ExecutionState.FAILED) {
						// in state failed already, no transition necessary any more
						break;
					}
					// unexpected state, go to failed
					else if (STATE_UPDATER.compareAndSet(this, current, ExecutionState.FAILED)) {
						LOG.error("Unexpected state in Task during an exception: " + current);
						break;
					}
					// else fall through the loop and 
				}
			}
			catch (Throwable tt) {
				String message = "FATAL - exception in task exception handler";
				LOG.error(message, tt);
				notifyFatalError(message, tt);
			}
		}
		finally {
			try {
				LOG.info("Freeing task resources for " + taskNameWithSubtask);
				
				// free the network resources
				network.unregisterTask(this);

				if (invokable != null) {
					memoryManager.releaseAll(invokable);
				}

				// remove all of the tasks library resources
				libraryCache.unregisterTask(jobId, executionId);

				// remove all files in the distributed cache
				removeCachedFiles(distributedCacheEntries, fileCache);

				notifyFinalState();
			}
			catch (Throwable t) {
				// an error in the resource cleanup is fatal
				String message = "FATAL - exception in task resource cleanup";
				LOG.error(message, t);
				notifyFatalError(message, t);
			}
		}
	}
