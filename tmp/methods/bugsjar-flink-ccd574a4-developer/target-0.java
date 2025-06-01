	public void deployToSlot(final SimpleSlot slot) throws JobException {
		// sanity checks
		if (slot == null) {
			throw new NullPointerException();
		}
		if (!slot.isAlive()) {
			throw new JobException("Target slot for deployment is not alive.");
		}

		// make sure exactly one deployment call happens from the correct state
		// note: the transition from CREATED to DEPLOYING is for testing purposes only
		ExecutionState previous = this.state;
		if (previous == SCHEDULED || previous == CREATED) {
			if (!transitionState(previous, DEPLOYING)) {
				// race condition, someone else beat us to the deploying call.
				// this should actually not happen and indicates a race somewhere else
				throw new IllegalStateException("Cannot deploy task: Concurrent deployment call race.");
			}
		}
		else {
			// vertex may have been cancelled, or it was already scheduled
			throw new IllegalStateException("The vertex must be in CREATED or SCHEDULED state to be deployed. Found state " + previous);
		}

		try {
			// good, we are allowed to deploy
			if (!slot.setExecutedVertex(this)) {
				throw new JobException("Could not assign the ExecutionVertex to the slot " + slot);
			}
			this.assignedResource = slot;
			this.assignedResourceLocation = slot.getInstance().getInstanceConnectionInfo();

			// race double check, did we fail/cancel and do we need to release the slot?
			if (this.state != DEPLOYING) {
				slot.releaseSlot();
				return;
			}
			
			if (LOG.isInfoEnabled()) {
				LOG.info(String.format("Deploying %s (attempt #%d) to %s", vertex.getSimpleName(),
						attemptNumber, slot.getInstance().getInstanceConnectionInfo().getHostname()));
			}
			
			final TaskDeploymentDescriptor deployment = vertex.createDeploymentDescriptor(attemptId, slot);
			
			// register this execution at the execution graph, to receive call backs
			vertex.getExecutionGraph().registerExecution(this);

			final Instance instance = slot.getInstance();
			Future<Object> deployAction = Patterns.ask(instance.getTaskManager(),
					new SubmitTask(deployment), new Timeout(timeout));

			deployAction.onComplete(new OnComplete<Object>(){

				@Override
				public void onComplete(Throwable failure, Object success) throws Throwable {
					if (failure != null) {
						if (failure instanceof TimeoutException) {
							markFailed(new Exception(
									"Cannot deploy task - TaskManager " + instance + " not responding.",
									failure));
						}
						else {
							markFailed(failure);
						}
					}
					else {
						if (success == null) {
							markFailed(new Exception("Failed to deploy the task to slot " + slot + ": TaskOperationResult was null"));
						}

						if (success instanceof TaskOperationResult) {
							TaskOperationResult result = (TaskOperationResult) success;

							if (!result.executionID().equals(attemptId)) {
								markFailed(new Exception("Answer execution id does not match the request execution id."));
							} else if (result.success()) {
								switchToRunning();
							} else {
								// deployment failed :(
								markFailed(new Exception("Failed to deploy the task " +
										getVertexWithAttempt() + " to slot " + slot + ": " + result
										.description()));
							}
						} else {
							markFailed(new Exception("Failed to deploy the task to slot " + slot +
									": Response was not of type TaskOperationResult"));
						}
					}
				}
			}, AkkaUtils.globalExecutionContext());
		}
		catch (Throwable t) {
			markFailed(t);
			ExceptionUtils.rethrow(t);
		}
	}
